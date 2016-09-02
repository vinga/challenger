package com.kameo.challenger;


import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.odb.ChallengeContractODB;
import com.kameo.challenger.odb.ChallengeContractStatus;
import com.kameo.challenger.odb.UserODB;
import com.kameo.challenger.services.ChallengerService;
import com.kameo.challenger.services.FakeDataService;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.MailService;
import com.kameo.challenger.utils.StringHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

@AutoConfigureDataJpa
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class InvitationsTest implements En {
    private Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

    @Inject
    private AnyDAO anyDao;
    @Inject
    ChallengerService challengerService;
    @Inject
    FakeDataService cmd;
    @Inject
    TestHelper testHelper;

    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }


    public InvitationsTest() {
        When("^I invite my friend to new challenge$", () -> {

            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();

            ChallengeContractODB cb = new ChallengeContractODB();
            cb.setLabel("Default challenge");
            cb.setFirst(myself);
            cb.setSecond(myFriend);

            challengerService.createNewChallenge(myself.getId(), cb);
        });



        Then("^my friend should see (\\d+) unanswered challenge invitation$", (Integer arg2) -> {
            UserODB myFriend = testHelper.myFriend();
            List<ChallengeContractODB> pending = challengerService.getPendingChallenges(myFriend.getId());
            Assert.assertEquals(arg2.longValue(), pending.size());
        });



        searchForUserByLoginScenario();
        inviteNonExistingUserByEmail();
        nonExistingUserConfirmsEmailInvitation();
    }






    private void searchForUserByLoginScenario() {
        When("^I search friend's name by login$", () -> {

        });
        Then("^I get all logins starting with provided texts$", () -> {
            cmd.createUsers("myFriend", "myFriend1", "myFriend2", "myFriend3");
            List<String> logins = challengerService.findUsersWithLoginsStartingWith("myFriend");
            Assert.assertEquals(4, logins.size());
        });

    }

    private void inviteNonExistingUserByEmail() {

        When("^I invite non existing email contact to new challenge$", () -> {
            String friendEmail = "myFriend@email.em";
            UserODB myself = testHelper.myself();

            ChallengeContractODB cb = new ChallengeContractODB();
            cb.setLabel("Default challenge");
            cb.setFirst(myself);
            cb.setSecondEmail(friendEmail);

            challengerService.createNewChallenge(myself.getId(), cb);
        });

        When("^I invite existing email contact to new challenge$", () -> {
            cmd.createUsers("myFriend");
            String friendEmail = "myFriend@email.em";
            UserODB myself = testHelper.myself();

            ChallengeContractODB cb = new ChallengeContractODB();
            cb.setLabel("Default challenge");
            cb.setFirst(myself);
            cb.setSecondEmail(friendEmail);

            challengerService.createNewChallenge(myself.getId(), cb);
        });

        Then("^He gets email notification$", () -> {
            Assert.assertEquals(1, MailService.messages.size());
            MailService.messages.clear();
        });
    }

    private void nonExistingUserConfirmsEmailInvitation() {
        Given("^I am not challenger user$", () -> {
            Assert.assertFalse(testHelper.myselfOptional().isPresent());
        });

        Given("^I received email invitation from my friend$", () -> {
            cmd.createUsers("myFriend");
            UserODB myFriend = anyDao.getOnlyOne(UserODB.class, u -> u.getLogin().equals("myFriend"));
            ChallengeContractODB cb = new ChallengeContractODB();
            cb.setLabel("Default challenge");
            cb.setFirst(myFriend);
            cb.setSecondEmail("myself@email.em");
            challengerService.createNewChallenge(myFriend.getId(), cb);
        });

        When("^I accept email link$", () -> {
            Assert.assertEquals(1, MailService.messages.size());
            MailService.Message mm = MailService.messages.get(0);

            String actionUrl = StringHelper.getFirstHrefValue(mm.getContent());
            System.out.println(actionUrl);

            int i = actionUrl.lastIndexOf("/");
            String uid = actionUrl.substring(i + 1);
            challengerService.acceptChallengeForExistingUser(uid, Optional.of("myself"));
            MailService.messages.clear();
        });

        Then("^my account will be created$", () -> {
            testHelper.myself();
        });

        Then("^my friend challenge will be accepted$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();

            anyDao.getOnlyOne(ChallengeContractODB.class, cc ->
                    cc.getFirst().equals(myFriend) && cc.getSecond().equals(myself)
                            && cc.getChallengeContractStatus() == ChallengeContractStatus.ACTIVE
            );


        });
    }
}
