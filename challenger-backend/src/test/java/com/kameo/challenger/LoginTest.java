package com.kameo.challenger;

import com.challenger.eviauth.config.DatabaseTestConfig;
import com.challenger.eviauth.config.ServicesLayerConfig;
import com.challenger.eviauth.odb.SessionODB;
import com.challenger.eviauth.odb.UserODB;
import com.challenger.eviauth.services.FakeDataService;
import com.challenger.eviauth.services.LoginService;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java8.En;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static com.challenger.eviauth.util.TestHelper.QUOTED_STR;

@AutoConfigureDataJpa
@ContextConfiguration(classes= {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class LoginTest implements En {
    private Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

    @Inject
    AnyDAO anyDao;
    @Inject
    LoginService loginService;
    @Inject
    FakeDataService cmd;


    public LoginTest() {
        loginScenerio();
    }

    String login;



    private void loginScenerio() {

        Given("^I have login "+QUOTED_STR+"$", (String login) -> {
            try {
                cmd.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.login=login;
          //  Optional<UserODB> opt = anyDao.streamAll(UserODB.class).where(u -> u.getLogin().equals(login)).findOne();
          //  Assert.assertTrue(opt.isPresent());
        });

        When("^I login to ESC application$", () -> {

                loginService.loginUser(login, cmd.data.appEsc.getApiKey());

        });

        Then("^I should success$", () -> {
            String login=this.login;
            UserODB opt = anyDao.getOnlyOne(UserODB.class, u -> u.getLogin().equals(login));
            long userId=opt.getId();
            SessionODB sess = anyDao.streamAll(SessionODB.class).where(s -> s.getUser().getId() == userId).getOnlyValue();
            System.out.println(sess);
        });

        Then("^I should fail", () -> {
            String login=this.login;

            UserODB opt = anyDao.getOnlyOne(UserODB.class, u -> u.getLogin().equals(login));
            long userId=opt.getId();
            Optional<SessionODB> sess = anyDao.streamAll(SessionODB.class).where(s -> s.getUser().getId() == userId ).findAny();
            Assert.assertFalse(sess.isPresent());
        });
    }

}
