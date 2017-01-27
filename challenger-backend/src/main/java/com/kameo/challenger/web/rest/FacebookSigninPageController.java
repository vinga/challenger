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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

//https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow/
//http://localhost:9080/oauth2/facebookSignIn
@Controller
public class FacebookSigninPageController {
    @Value("${oauth2.facebook.clientId}")
    private String oauth2facebookClientId;
    @Value("${oauth2.facebook.clientSecret}")
    private String oauth2facebookClientSecret;
    @Value("${serverUrl}")
    private String serverUrl;
    private final ServerConfig serverConfig;
    private final AccountDAO accountDAO;
    private final ConfirmationLinkDAO confirmationLinkDao;

    @Inject
    public FacebookSigninPageController(ServerConfig serverConfig, AccountDAO accountDAO, ConfirmationLinkDAO confirmationLinkDao) {
        this.serverConfig = serverConfig;
        this.accountDAO = accountDAO;
        this.confirmationLinkDao = confirmationLinkDao;
    }

    @RequestMapping("/oauth2/facebookSignIn")
    public RedirectView greeting(HttpServletResponse httpServletResponse) {
        String callback = getCallbackUrl();
        System.out.println("callback " + callback);
        StringBuilder oauthUrl = new StringBuilder().append("https://www.facebook.com/v2.8/dialog/oauth")
                                                    .append("?client_id=")
                                                    .append(oauth2facebookClientId) // the client id from the api console
                                                    // registration
                                                    .append("&response_type=code")
                                                    .append("&scope=public_profile,email") // scope is the api permissions we are requesting
                                                    .append("&redirect_uri=") // the servlet that google redirects to after authorization
                                                    .append(callback);
        //.append("&state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id")
        // .append("&access_type=offline") // here we are asking to access to user's data while they are not signed in
        // .append("&approval_prompt=force"); // this requires them to verify which account to use, if they are already signed in
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(oauthUrl.toString());
        return redirectView;
    }

    @RequestMapping("/oauth2/facebookCallback")
    public
    @ResponseBody
    RedirectView callback(@RequestParam("code") String code,
                          @RequestParam(value = "error", required = false) String error) {

        /*YOUR_REDIRECT_URI?
                error_reason=user_denied
                        &error=access_denied
                        &error_description=The+user+denied+your+request.*/
        //error_reason=user_denied
         //       &error=access_denied

        if (error != null) {
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(serverUrl);
            return redirectView;
        }
        /*if (req.getParameter("error") != null) {
            resp.getWriter().println(req.getParameter("error"));
            return;
        }*/
        try {
            String accessToken = exchangeCodeForAccessToken(code);
            String json = HttpUtil.get("https://graph.facebook.com/me?fields=name,email&access_token=" + accessToken);

             /*
            {"name":"FirstName LastName","email":"...\u0040...","id":"12345..."}
            */
            Map<String, String> userDataObjects = JsonUtil.asMap(json);
            String googleId = userDataObjects.get("id");
            String email = userDataObjects.get("email");
            email = email.replace("\u0040", "@");
            System.out.println("new email " + email);
            boolean isEmailVerified = true; //always verified: http://stackoverflow.com/questions/14280535/is-it-possible-to-check-if-an-email-is-confirmed-on-facebook
            final UserODB user = accountDAO.getOrCreateOauth2FacebookUser(googleId, email, isEmailVerified);
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
        return serverUrl + "oauth2/facebookCallback";
    }

    private String exchangeCodeForAccessToken(String code) throws IOException {
        String body = HttpUtil.post("https://graph.facebook.com/v2.8/oauth/access_token", ImmutableMap.<String, String>builder()
                .put("code", code)
                .put("client_id", oauth2facebookClientId)
                .put("client_secret", oauth2facebookClientSecret)
                .put("redirect_uri", getCallbackUrl())
                .build());
        Map<String, String> jsonObject = JsonUtil.asMap(body);
        return jsonObject.get("access_token");
    }
}