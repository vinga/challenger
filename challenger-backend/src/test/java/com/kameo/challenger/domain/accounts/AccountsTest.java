package com.kameo.challenger.domain.accounts;


import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkODB;
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkType;
import com.kameo.challenger.domain.accounts.db.UserStatus;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB;
import com.kameo.challenger.domain.challenges.db.ChallengeStatus;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.DateUtil;
import com.kameo.challenger.utils.auth.jwt.AbstractAuthFilter;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.hibernate.engine.spi.Managed;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

//@AutoConfigureDataJpa
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class AccountsTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private TestHelper testHelper;
    @Inject
    private ConfirmationLinkLogic confirmationLinkLogic;
    @Inject
    private AccountDAO accountDao;

    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }


    private boolean invalidCredentials;
    private int noOfBadLoginTimes;
    private boolean registerResult;

    public AccountsTest() {

        When("^I login with wrong password$", () -> {

            try {
                accountDao.login(testHelper.myself().getLogin(), "otherpass");
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
                    accountDao.login(testHelper.myself().getLogin(), "otherpass");
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

            Assert.assertTrue(dueDate.after(DateUtil.addMinutes(new Date(), 19)));
            Assert.assertTrue(dueDate.before(DateUtil.addMinutes(new Date(), 21)));

        });

        Then("^my account is still not blocked$", () -> {
            Assert.assertTrue(invalidCredentials);
            Assert.assertEquals(UserStatus.ACTIVE, testHelper.myself().getUserStatus());
        });


        Given("^I no invitation has been sent to me email$", () -> {
            String myEmail = "myself@email.em";

            Optional<ChallengeParticipantODB> cco = anyDao.streamAll(ChallengeParticipantODB.class)
                    .where(cc -> !cc.getChallenge().getCreatedBy().equals(cc.getUser()) && cc.getUser().getEmail().equals(myEmail) || cc
                            .getUser().getEmail().equals(myEmail)).findAny();
            Assert.assertTrue(!cco.isPresent());
        });

        When("^I register with that email$", () ->
                registerResult = accountDao.registerUser("myself", "myselfpass", "myself@email.em")
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
                    .where(c -> "myself@email.em".equals(c.getEmail()) && c
                            .getConfirmationLinkType() == ConfirmationLinkType.EMAIL_CONFIRMATION)
                    .getOnlyValue();
            Assert.assertTrue(!testHelper.getSentMessagesList().isEmpty());
            testHelper.getSentMessagesList().clear();

            try {
                accountDao.login("myself", "myselfpass");
                Assert.fail();
            } catch (AbstractAuthFilter.AuthException e) {
                //ignore
            }
            confirmationLinkLogic.confirmLinkByUid(onlyValue.getUid());

            try {
                accountDao.login("myself", "myselfpass");

            } catch (AbstractAuthFilter.AuthException e) {
                Assert.fail();
            }

        });



        When("^I put my email into reset password option$", () -> accountDao.sendResetMyPasswordLink(testHelper.myself().email));

        Then("^I received email with password reset link$", () -> {
            ConfirmationLinkODB onlyValue = anyDao.streamAll(ConfirmationLinkODB.class)
                    .where(c -> "myself@email.em".equals(c.getEmail()) && c
                            .getConfirmationLinkType() == ConfirmationLinkType.PASSWORD_RESET)
                    .getOnlyValue();
            Assert.assertTrue(!testHelper.getSentMessagesList().isEmpty());
            testHelper.getSentMessagesList().clear();
        });

        Then("^I click on password reset link$", () -> {
            ConfirmationLinkODB onlyValue = anyDao.streamAll(ConfirmationLinkODB.class)
                    .where(c -> "myself@email.em".equals(c.getEmail()) && c
                            .getConfirmationLinkType() == ConfirmationLinkType.PASSWORD_RESET)
                    .getOnlyValue();


            try {
                accountDao.login("myself", "myselfpass");
            } catch (AbstractAuthFilter.AuthException e) {
                Assert.fail();
            }

        });

        Then("^I can set my new password$", () -> {
            ConfirmationLinkODB onlyValue = anyDao.streamAll(ConfirmationLinkODB.class)
                    .where(c -> "myself@email.em".equals(c.getEmail()) && c
                            .getConfirmationLinkType() == ConfirmationLinkType.PASSWORD_RESET)
                    .getOnlyValue();
            confirmationLinkLogic.resetPassword(onlyValue.getUid(),"newPass");
            try {
                accountDao.login("myself", "myselfpass");
                Assert.fail();
            } catch (AbstractAuthFilter.AuthException e) {
                //its OK
            }
            try {
                accountDao.login("myself", "newPass");

            } catch (AbstractAuthFilter.AuthException e) {
                Assert.fail();
            }

        });
    }


}
