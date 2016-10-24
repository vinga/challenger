package com.kameo.challenger.utils.auth.jwt;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


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


/*        if (true) {
            chain.doFilter(req,res);
            return;
        }*/


        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;
        if (isResourceANewTokenGenerator(httpReq)) {
            try {
                E tokenInfo = createNewToken(httpReq, httpRes);

                printTokenToResponse(httpRes, tokenInfo);


            } catch (AuthException ex) {
                onAuthException(ex, httpReq, httpRes, chain);
            }
        } else if (isResourceAuthorizationRequired(httpReq)) {
            try {
                String auth = httpReq.getHeader("Authorization");
                if (Strings.isNullOrEmpty(auth)) {
                    throw new IllegalAccessException("Unauthorized");
                }
                String tokens = auth.substring("Bearer ".length());


                List<String> tokensStringList=Lists.newArrayList(Splitter.on(" ").omitEmptyStrings().trimResults().split(tokens));
                List<E> tokensList=Lists.newArrayList();
                for (String token: tokensStringList) {
                    E tokenInfo = verifier.verifyToken(token);
                    if (tokenInfo.getExpires().isBeforeNow()) {
                        onTokenExpired(httpReq, httpRes, chain);
                    }
                    else tokensList.add(tokenInfo);
                }



                //Authorization:Bearer eyJhbjoxNNeu6vks-xXrAN9RJ77GnbzeC5Q eyJhbjoxNNeu6vks-xXrAN9RJ77GnbzeC5Q eyJhbjoxNNeu6vks-xXrAN9RJ77GnbzeC5Q



                onTokenValidated(tokensList, httpReq, httpRes, chain);


            } catch (Exception ex) {
                ex.printStackTrace();
                unauthorized(httpRes);
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    private void printTokenToResponse(HttpServletResponse httpRes, E tokenInfo) throws IOException {
        String newToken = signer
                .createJsonWebToken(tokenInfo, tokenInfo.getExpires().toDate());
        httpRes.addHeader("Content-Type", "text/html; charset=utf-8");
        httpRes.getWriter().print(newToken);
        httpRes.getWriter().flush();
    }

    /**
     * Example implementation:
     * return "/api/newToken".equals(req.getPathInfo());
     *
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
    protected abstract E createNewToken(HttpServletRequest req, HttpServletResponse resp) throws AuthException;


    /**
     * User is authorized, do
     *
     * @param ti
     */
    protected abstract void setRequestScopeVariable(List<E> ti);


    protected void onTokenValidated(List<E> ti, HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
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

    protected void onAuthException(AuthException ex, HttpServletRequest req, HttpServletResponse response, FilterChain chain) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(ex.getMessage());
        response.getWriter().flush();
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

    public static class AuthException extends Exception {


        public AuthException(String message) {
            super(message);
        }

    }



}

