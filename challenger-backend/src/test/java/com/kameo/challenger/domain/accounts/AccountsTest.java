package com.kameo.challenger.domain.accounts;


import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.IAccountRestService.ConfirmationLinkRequestDTO;
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkODB;
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkType;
import com.kameo.challenger.domain.accounts.db.UserStatus;
import com.kameo.challenger.domain.challenges.ChallengeDAO;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB;
import com.kameo.challenger.domain.challenges.db.ChallengeStatus;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.auth.jwt.AbstractAuthFilter;
import com.kameo.challenger.utils.auth.jwt.JWTService.AuthException;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

//@AutoConfigureDataJpa
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class AccountsTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private TestHelper testHelper;
    @Inject
    private ConfirmationLinkDAO confirmationLinkLogic;
    @Inject
    private AccountDAO accountDao;
    @Inject
    private ChallengeDAO challengerDao;

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
            } catch (AuthException ex) {
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
                } catch (AuthException ex) {
                    invalidCredentials = true;
                }
            }
        });

        Then("^I got information that my account is blocked for 20 min$", () -> {
            Assert.assertTrue(invalidCredentials);
            Assert.assertEquals(noOfBadLoginTimes, testHelper.myself().getFailedLoginsNumber());
            Assert.assertEquals(UserStatus.SUSPENDED, testHelper.myself().getUserStatus());
            LocalDateTime dueDate = testHelper.myself().getSuspendedDueDate();


            Assert.assertTrue(dueDate.isAfter(LocalDateTime.now().minusMinutes(20)));
            Assert.assertTrue(dueDate.isBefore(LocalDateTime.now().plusMinutes(21)));

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
                registerResult = accountDao.registerUser("myself", "myselfpass", "myself@email.em", null).getError() == null
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
                                                  .where(c -> "myself@email.em".equals(c.getUser().getEmail()) && c
                                                          .getConfirmationLinkType() == ConfirmationLinkType.EMAIL_CONFIRMATION)
                                                  .getOnlyValue();
            Assert.assertTrue(!testHelper.getSentMessagesList().isEmpty());
            testHelper.getSentMessagesList().clear();

            try {
                accountDao.login("myself", "myselfpass");
                Assert.fail();
            } catch (AuthException e) {
                //ignore
            }
            ConfirmationLinkRequestDTO req=new ConfirmationLinkRequestDTO();
            confirmationLinkLogic.confirmLink(onlyValue.getUid(), req, accountDao, challengerDao);

            try {
                accountDao.login("myself", "myselfpass");

            } catch (AuthException e) {
                Assert.fail();
            }

        });


        When("^I put my email into reset password option$", () -> accountDao.sendResetMyPasswordLink(testHelper.myself().email));

        Then("^I received email with password reset link$", () -> {
            ConfirmationLinkODB onlyValue = anyDao.streamAll(ConfirmationLinkODB.class)
                                                  .where(c -> "myself@email.em".equals(c.getUser().getEmail()) && c
                                                          .getConfirmationLinkType() == ConfirmationLinkType.PASSWORD_RESET)
                                                  .getOnlyValue();
            Assert.assertTrue(!testHelper.getSentMessagesList().isEmpty());
            testHelper.getSentMessagesList().clear();
        });

        Then("^I click on password reset link$", () -> {
            ConfirmationLinkODB onlyValue = anyDao.streamAll(ConfirmationLinkODB.class)
                                                  .where(c -> "myself@email.em".equals(c.getUser().getEmail()) && c
                                                          .getConfirmationLinkType() == ConfirmationLinkType.PASSWORD_RESET)
                                                  .getOnlyValue();


            try {
                accountDao.login("myself", "myselfpass");
            } catch (AuthException e) {
                Assert.fail();
            }

        });

        Then("^I can set my new password$", () -> {
            ConfirmationLinkODB onlyValue = anyDao.streamAll(ConfirmationLinkODB.class)
                                                  .where(c -> "myself@email.em".equals(c.getUser().getEmail()) && c
                                                          .getConfirmationLinkType() == ConfirmationLinkType.PASSWORD_RESET)
                                                  .getOnlyValue();

            ConfirmationLinkRequestDTO req=new ConfirmationLinkRequestDTO(null, "newPass");
            confirmationLinkLogic.confirmLink(onlyValue.getUid(), req, accountDao, challengerDao);


            try {
                accountDao.login("myself", "myselfpass");
                Assert.fail();
            } catch (AuthException e) {
                //its OK
            }
            try {
                accountDao.login("myself", "newPass");

            } catch (AuthException e) {
                Assert.fail();
            }

        });
    }


}
