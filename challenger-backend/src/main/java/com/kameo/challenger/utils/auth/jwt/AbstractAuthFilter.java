package com.kameo.challenger.utils.auth.jwt;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.kameo.challenger.utils.auth.jwt.JWTService.AuthException;
import com.kameo.challenger.utils.auth.jwt.JWTService.TokenExpiredException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public abstract class AbstractAuthFilter<E extends TokenInfo> implements Filter {
    private JWTService<E> jwtService;

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        jwtService = createJWTService(arg0);
    }

    protected abstract JWTService<E> createJWTService(FilterConfig arg0);

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
                    chain.doFilter(req, res);
                    return;
                    //throw new IllegalAccessException("Unauthorized"); ignore, rely on spring security instead
                }
                String tokens = auth.substring("Bearer " .length());
                List<String> tokensStringList = Lists.newArrayList(Splitter.on(" ").omitEmptyStrings().trimResults().split(tokens));
                List<E> tokensList = Lists.newArrayList();
                for (String token : tokensStringList) {
                    E tokenInfo = jwtService.verifyToken(token);
                    if (tokenInfo.getExpires().isBeforeNow()) {
                        onTokenExpired(httpReq, httpRes, chain);
                    } else tokensList.add(tokenInfo);
                }
                if (isResourceARenewTokenGenerator(httpReq)) {
                    setRequestScopeVariable(tokensList);
                    E tokenInfo = renewToken(httpReq, httpRes);
                    printTokenToResponse(httpRes, tokenInfo);
                    return;
                }
                //Authorization:Bearer eyJhbjoxNNeu6vks-xXrAN9RJ77GnbzeC5Q eyJhbjoxNNeu6vks-xXrAN9RJ77GnbzeC5Q eyJhbjoxNNeu6vks-xXrAN9RJ77GnbzeC5Q
                onTokenValidated(tokensList, httpReq, httpRes, chain);
            } catch (TokenExpiredException ex) {
                System.out.println("TOKEN expired....");
                ex.printStackTrace();
                unauthorized(httpRes, true);
            } catch (Exception ex) {
                ex.printStackTrace();
                unauthorized(httpRes, false);
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    private void printTokenToResponse(HttpServletResponse httpRes, E tokenInfo) throws IOException {
        String newToken = tokenToString(tokenInfo);
        httpRes.addHeader("Content-Type", "text/html; charset=utf-8");
        httpRes.getWriter().print(newToken);
        httpRes.getWriter().flush();
    }

    public String tokenToString(E tokenInfo) {
        return jwtService.tokenToString(tokenInfo);
    }

    /**
     * Example implementation:
     * return "/api/newToken".equals(req.getPathInfo());
     *
     * @return
     */
    protected abstract boolean isResourceANewTokenGenerator(HttpServletRequest req);

    protected abstract boolean isResourceARenewTokenGenerator(HttpServletRequest req);

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
        System.out.println("ON TOKEN EXPIRED -HERE");
        response.setHeader("jwt-status", "expired");
        unauthorized(response, true);
    }

    protected void onNotAuthorized(HttpServletRequest req, HttpServletResponse response, FilterChain chain) throws IOException {
        unauthorized(response, false);
    }

    protected void onAuthException(AuthException ex, HttpServletRequest req, HttpServletResponse response, FilterChain chain) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(ex.getMessage());
        response.getWriter().flush();
    }

    private void unauthorized(HttpServletResponse response, boolean tokenExpired) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (tokenExpired)
            response.getWriter().print("Unauthorized-tokenExpired");
        else
            response.getWriter().print("Unauthorized");
        response.getWriter().flush();
    }

    @Override
    public void destroy() {
    }

    protected abstract E renewToken(HttpServletRequest req, HttpServletResponse resp) throws AuthException;
}

