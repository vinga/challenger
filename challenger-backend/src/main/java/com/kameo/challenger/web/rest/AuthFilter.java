package com.kameo.challenger.web.rest;

import com.kameo.challenger.config.ServerConfig;
import com.kameo.challenger.domain.accounts.AccountDAO;
import com.kameo.challenger.utils.DateUtil;
import com.kameo.challenger.utils.ReflectionUtils;
import com.kameo.challenger.utils.auth.jwt.AbstractAuthFilter;
import com.kameo.challenger.utils.auth.jwt.JWTServiceConfig;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthFilter extends AbstractAuthFilter<ChallengerSess> {
    private final ChallengerSess myTokenInfo;
    private final Provider<MultiUserChallengerSess> multiTokenInfos;
    private final AccountDAO accountDao;
    private ServerConfig serverConfig;

    @Inject
    public AuthFilter(ChallengerSess myTokenInfo, Provider<MultiUserChallengerSess> multiTokenInfos, AccountDAO accountDao, ServerConfig serverConfig) {
        this.myTokenInfo = myTokenInfo;
        this.multiTokenInfos = multiTokenInfos;
        this.accountDao = accountDao;
        this.serverConfig = serverConfig;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (serverConfig.isCrossDomain()) {
            HttpServletResponse resp = (HttpServletResponse) res;
            resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            resp.addHeader("Access-Control-Allow-Credentials", "true");
            if (((HttpServletRequest) req).getMethod().equals("OPTIONS")) {
                resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
                resp.addHeader("Access-Control-Allow-Headers", "Authorization, Content-type");
                return;
            }
        }
        super.doFilter(req, res, chain);
    }

    @Override
    protected boolean isResourceANewTokenGenerator(HttpServletRequest req) {
        return "/accounts/newToken".equals(req.getPathInfo());
    }

    protected boolean isResourceARenewTokenGenerator(HttpServletRequest req) {
        return "/accounts/renewToken".equals(req.getPathInfo());
    }

    @Override
    protected boolean isResourceAuthorizationRequired(HttpServletRequest req) {
        if (req.getPathInfo() == null)
            return false;
        System.out.println(req.getPathInfo());
        return
                !(
                        req.getPathInfo().startsWith("/accounts/passwordReset") ||
                        req.getPathInfo().startsWith("/accounts/register") || req.getPathInfo().startsWith("/accounts/confirmationLinks/") || req.getPathInfo().contains("swagger"));
    }

    @Override
    protected JWTServiceConfig getJWTServiceConfig(FilterConfig fc) {
        return new JWTServiceConfig<>("signingkeytemporaryherebutwillbemovedtoouterfile"
                .getBytes(), "Kameo", "ChallengerUsers", ChallengerSess.class);
    }

    @Override
    protected ChallengerSess renewToken(HttpServletRequest req, HttpServletResponse resp) throws AuthException {
        String login = req.getParameter("login");
        //System.out.println("LOGIN "+login+" "+myTokenInfo.getUserId()+" "+accountDao.getUserIdByLogin(login));
        if (myTokenInfo.getUserId() != accountDao.getUserIdByLogin(login))
            throw new IllegalArgumentException();
        ChallengerSess td = new ChallengerSess();
        td.setUserId(myTokenInfo.getUserId());
        td.setExpires(new DateTime(DateUtil.addMinutes(new Date(), 15)));
        return td;
    }

    @Override
    protected ChallengerSess createNewToken(HttpServletRequest req, HttpServletResponse resp) throws AuthException {
        String login = req.getParameter("login");
        String pass = req.getParameter("pass");
        return createNewTokenFromCredentials(login, pass);
    }

    @NotNull
    private ChallengerSess createNewTokenFromCredentials(String login, String pass) throws AuthException {
        long userId = accountDao.login(login, pass);
        ChallengerSess td = new ChallengerSess();
        td.setUserId(userId);
        td.setExpires(new DateTime(DateUtil.addMinutes(new Date(), 15)));
        return td;
    }

    @NotNull
    public ChallengerSess createNewTokenFromUserId(long userId) throws AuthException {
        ChallengerSess td = new ChallengerSess();
        td.setUserId(userId);
        td.setExpires(new DateTime(DateUtil.addMinutes(new Date(), 15)));
        return td;
    }


    @Override
    protected void setRequestScopeVariable(List<ChallengerSess> ti) {
        if (ti.size() == 1)
            ReflectionUtils.copy(ti.get(0), this.myTokenInfo);
        else {
            multiTokenInfos.get().setUserIds(ti.stream().map(ChallengerSess::getUserId).collect(Collectors.toSet()));
        }
    }
}
