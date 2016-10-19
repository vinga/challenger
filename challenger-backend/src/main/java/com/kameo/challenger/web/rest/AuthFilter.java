package com.kameo.challenger.web.rest;

import com.kameo.challenger.config.ServerConfig;
import com.kameo.challenger.domain.accounts.AccountDAO;
import com.kameo.challenger.utils.ReflectionUtils;
import com.kameo.challenger.utils.auth.jwt.AbstractAuthFilter;
import com.kameo.challenger.utils.auth.jwt.JWTServiceConfig;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.*;
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

    @Inject
    public AuthFilter(ChallengerSess myTokenInfo, Provider<MultiUserChallengerSess> multiTokenInfos, AccountDAO accountDao) {
        this.myTokenInfo = myTokenInfo;
        this.multiTokenInfos = multiTokenInfos;
        this.accountDao = accountDao;
    }


    @Override
    protected boolean isResourceANewTokenGenerator(HttpServletRequest req) {
        return req.getPathInfo().equals("/api/newToken");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (ServerConfig.isCrossDomain()) {
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
    protected JWTServiceConfig getJWTServiceConfig(FilterConfig fc) {
        return new JWTServiceConfig<>("signingkeytemporaryherebutwillbemovedtoouterfile"
                .getBytes(), "Kameo", "ChallengerUsers", ChallengerSess.class);
    }

    @Override
    protected ChallengerSess createNewToken(HttpServletRequest req, HttpServletResponse resp) throws AuthException {
        String login = req.getParameter("login");
        String pass = req.getParameter("pass");
        long userId = accountDao.login(login, pass);
        ChallengerSess td = new ChallengerSess();
        td.setUserId(userId);
        td.setExpires(new DateTime(DateUtils.addMinutes(new Date(), 15)));
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
