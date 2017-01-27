package com.kameo.challenger.web.rest;

import com.google.common.collect.ImmutableMap;
import com.kameo.challenger.config.ServerConfig;
import com.kameo.challenger.domain.accounts.AccountDAO;
import com.kameo.challenger.domain.accounts.ConfirmationLinkDAO;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.utils.HttpUtil;
import com.kameo.challenger.utils.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

//http://highaltitudedev.blogspot.com/2013/10/google-oauth2-with-jettyservlets.html
//http://localhost:9080/oauth2/googleSignIn
@Controller
public class GoogleSigninPageController {
    @Value("${oauth2.google.clientId}")
    private String oauth2googleClientId;
    @Value("${oauth2.google.clientSecret}")
    private String oauth2googleClientSecret;
    @Value("${serverUrl}")
    private String serverUrl;

    private final ServerConfig serverConfig;
    private final AccountDAO accountDAO;
    private final ConfirmationLinkDAO confirmationLinkDao;

    @Inject
    public GoogleSigninPageController(ServerConfig serverConfig, AccountDAO accountDAO, ConfirmationLinkDAO confirmationLinkDao) {
        this.serverConfig = serverConfig;
        this.accountDAO = accountDAO;
        this.confirmationLinkDao = confirmationLinkDao;
    }

    @RequestMapping("/oauth2/googleSignIn")
    public RedirectView greeting(HttpServletResponse httpServletResponse) {
        StringBuilder oauthUrl = new StringBuilder().append("https://accounts.google.com/o/oauth2/auth")
                                                    .append("?client_id=")
                                                    .append(oauth2googleClientId) // the client id from the api console
                                                    // registration
                                                    .append("&response_type=code")
                                                    .append("&scope=openid%20email") // scope is the api permissions we are requesting
                                                    .append("&redirect_uri=") // the servlet that google redirects to after authorization
                                                    .append(getCallbackUrl())
                                                    //.append("&state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id")
                                                    .append("&access_type=offline") // here we are asking to access to user's data while they are not signed in
                                                   ;// .append("&approval_prompt=force"); // this requires them to verify which account to use, if they are already signed in
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(oauthUrl.toString());
        return redirectView;
    }

    @RequestMapping("/oauth2/googleCallback")
    public RedirectView callback(@RequestParam("code") String code, @RequestParam(value = "error", required = false) String error) {
        /*if (req.getParameter("error") != null) {
            resp.getWriter().println(req.getParameter("error"));
            return;
        }*/
        if (error != null) {
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(serverUrl);
            return redirectView;
        }
        try {
            String accessToken = exchangeCodeForAccessToken(code);
            String json = HttpUtil.get(new StringBuilder("https://www.googleapis.com/oauth2/v1/userinfo?access_token=").append(accessToken).toString());

            /*
                {
                "id": "12345",
                    "email": "email...",
                    "verified_email": true,
                    "name": "...",
                    "given_name": "...",
                    "family_name": "...",
                    "link": "https://plus.google.com/...",
                    "picture": "https://lh3.googleusercontent.com/.....jpg",
                    "gender": "female"
            }*/
            Map<String, String> userDataObjects = JsonUtil.asMap(json);
            String googleId = userDataObjects.get("id");
            String email = userDataObjects.get("email");
            boolean isEmailVerified = Boolean.parseBoolean(userDataObjects.get("verified_email"));
            final UserODB user = accountDAO.getOrCreateOauth2GoogleUser(googleId, email, isEmailVerified);
            String link = confirmationLinkDao.createOauth2LoginLink(user.getId());
            String callback2 = serverConfig.getConfirmEmailInvitationPattern(link);
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(callback2);
            return redirectView;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @NotNull
    private String getCallbackUrl() {
        return serverUrl + "oauth2/googleCallback";
    }

    private String exchangeCodeForAccessToken(String code) throws IOException {
        String body = HttpUtil.post("https://accounts.google.com/o/oauth2/token", ImmutableMap.<String, String>builder()
                .put("code", code)
                .put("client_id", oauth2googleClientId)
                .put("client_secret", oauth2googleClientSecret)
                .put("redirect_uri", getCallbackUrl())
                .put("grant_type", "authorization_code").build());
        return JsonUtil.asMap(body).get("access_token");
    }
}