package com.kameo.challenger.utils.auth.jwt;

import com.google.common.base.Strings;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;


public abstract class AbstractAuthFilter<E extends TokenInfo> implements Filter {
    private JWTSigner signer;
    private JWTVerifier<E> verifier;

    @Override
    public void init(FilterConfig arg0) throws ServletException {

        JWTServiceConfig sc = getJWTServiceConfig(arg0);
        this.signer = new JWTSigner(sc);
        this.verifier = new JWTVerifier<>(sc, sc.getTokenInfoClass());

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;
        if (isResourceANewTokenGenerator(httpReq)) {
            Optional<E> otokenInfo = createNewToken(httpReq, httpRes);
            if (otokenInfo.isPresent()) {
                String newToken = signer.createJsonWebToken(otokenInfo.get(), otokenInfo.get().getExpires().toDate());
                httpRes.addHeader("Content-Type", "text/html; charset=utf-8");
                httpRes.getWriter().print(newToken);
                httpRes.getWriter().flush();
            } else
                onNotAuthorized(httpReq, httpRes, chain);
        } else if (isResourceAuthorizationRequired(httpReq)) {
            try {
                String auth = httpReq.getHeader("Authorization");
                if (Strings.isNullOrEmpty(auth)) {
                    throw new IllegalAccessException("Unauthorized");
                }
                String token = auth.substring("Bearer ".length());
                TokenInfo tokenInfo = verifier.verifyToken(token);
                if (tokenInfo.getExpires().isBeforeNow()) {
                    onTokenExpired(httpReq, httpRes, chain);
                } else {
                    onTokenValidated(tokenInfo, httpReq, httpRes, chain);
                }

            } catch (IllegalAccessException ex) {
               // ex.printStackTrace();
                unauthorized(httpRes);
            } catch (Exception ex) {
                ex.printStackTrace();
                unauthorized(httpRes);
            }
        }
    }

    /**
     * Example implementation:
     * return "/api/newToken".equals(req.getPathInfo());
     * @param req
     * @return
     */
    protected abstract boolean isResourceANewTokenGenerator(HttpServletRequest req);

    protected boolean isResourceAuthorizationRequired(HttpServletRequest req) {
        return true;
    }

    /**
     * Example implementation:
     * String login=req.getParameter("login");
     * String pass=req.getParameter("pass");
     * TokenInfo td=new TokenInfoImpl();
     * td.setUserId(//id taken from db, etc)
     *
     * @param req
     * @param resp
     * @return
     */
    protected abstract Optional<E> createNewToken(HttpServletRequest req, HttpServletResponse resp);


    /**
     * User is authorized, do
     * @param ti
     */
    protected abstract void setRequestScopeVariable(TokenInfo ti);

    protected void onTokenValidated(TokenInfo ti, HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        setRequestScopeVariable(ti);
        chain.doFilter(req, res);
    }

    protected void onTokenExpired(HttpServletRequest req, HttpServletResponse response, FilterChain chain) throws IOException {
        response.setHeader("jwt-status", "expired");
        unauthorized(response);
    }
    protected void onNotAuthorized(HttpServletRequest req, HttpServletResponse response, FilterChain chain) throws IOException {
        unauthorized(response);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print("Unauthorized");
        response.getWriter().flush();
    }

    @Override
    public void destroy() {
    }

    protected abstract JWTServiceConfig getJWTServiceConfig(FilterConfig fc);



}

