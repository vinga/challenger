package com.kameo.challenger;


import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.logic.ConfirmationLinkLogic;
import com.kameo.challenger.logic.LoginLogic;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.auth.jwt.AbstractAuthFilter;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;

@AutoConfigureDataJpa
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class LoginTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private TestHelper testHelper;
    @Inject
    private ConfirmationLinkLogic confirmationLinkLogic;
    @Inject
    private LoginLogic loginLogic;

    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }


    private boolean invalidCredentials;
    private int noOfBadLoginTimes;
    private boolean registerResult;

    public LoginTest() {

        When("^I login with wrong password$", () -> {

            try {
                loginLogic.login(testHelper.myself().getLogin(), "otherpass");
                invalidCredentials = false;
                Assert.fail("Cannot login with wrong password");
            } catch (AbstractAuthFilter.AuthException ex) {
                Assert.assertEquals("Wrong credentials", ex.getMessage());
                invalidCredentials = true;
            }
        });

        Then("^I got information that my credentials are invalid$", () ->
                Assert.assertTrue(invalidCredentials)
        );


        When("^I login with wrong password (\\d+) times$", (Integer arg1) -> {
            invalidCredentials = false;
            noOfBadLoginTimes = arg1;
            for (int i = 0; i < arg1; i++) {
                try {
                    loginLogic.login(testHelper.myself().getLogin(), "otherpass");
                    Assert.fail("Cannot login with wrong password");
                } catch (AbstractAuthFilter.AuthException ex) {
                    invalidCredentials = true;
                }
            }
        });

        Then("^I got information that my account is blocked for 20 min$", () -> {
            Assert.assertTrue(invalidCredentials);
            Assert.assertEquals(noOfBadLoginTimes, testHelper.myself().getFailedLoginsNumber());
            Assert.assertEquals(UserStatus.SUSPENDED, testHelper.myself().getUserStatus());
            Date dueDate = testHelper.myself().getSuspendedDueDate();

            Assert.assertTrue(dueDate.after(DateUtils.addMinutes(new Date(), 19)));
            Assert.assertTrue(dueDate.before(DateUtils.addMinutes(new Date(), 21)));

        });

        Then("^my account is still not blocked$", () -> {
            Assert.assertTrue(invalidCredentials);
            Assert.assertEquals(UserStatus.ACTIVE, testHelper.myself().getUserStatus());
        });


        Given("^I no invitation has been sent to me email$", () -> {
            String myEmail = "myself@email.em";
            Optional<ChallengeODB> cco = anyDao.streamAll(ChallengeODB.class)
                                                       .where(cc -> cc.getFirst().getEmail().equals(myEmail) || cc
                                                               .getSecond().getEmail().equals(myEmail)).findAny();
            Assert.assertTrue(!cco.isPresent());
        });

        When("^I register with that email$", () ->
                registerResult = loginLogic.registerUser("myself", "myselfpass", "myself@email.em")
        );

        Then("^I don't have to confirm my email before I can login succesfully$", () -> {
            Assert.assertEquals(UserStatus.ACTIVE, testHelper.myself().getUserStatus());
            Assert.assertTrue(registerResult);
            Assert.assertTrue(testHelper.getSentMessagesList().isEmpty());

        });

        Given("^I not confirmed it yet$", () ->
                Assert.assertEquals(ChallengeStatus.WAITING_FOR_ACCEPTANCE, anyDao
                        .streamAll(ChallengeODB.class).getOnlyValue().getChallengeStatus())
        );

        Then("^I have to confirm my email before I can login succesfully$", () -> {
            ConfirmationLinkODB onlyValue = anyDao.streamAll(ConfirmationLinkODB.class)
                                                  .where(c -> c.getEmail().equals("myself@email.em") && c
                                                          .getConfirmationLinkType() == ConfirmationLinkType.EMAIL_CONFIRMATION)
                                                  .getOnlyValue();
            Assert.assertTrue(!testHelper.getSentMessagesList().isEmpty());
            testHelper.getSentMessagesList().clear();

            try {
                loginLogic.login("myself", "myselfpass");
                Assert.fail();
            } catch (AbstractAuthFilter.AuthException e) {
                //ignore
            }
            confirmationLinkLogic.confirmLinkByUid(onlyValue.getUid());

            try {
                loginLogic.login("myself", "myselfpass");

            } catch (AbstractAuthFilter.AuthException e) {
                Assert.fail();
            }

        });
    }


}
