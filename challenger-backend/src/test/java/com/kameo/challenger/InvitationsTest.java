package com.kameo.challenger;


import com.google.common.collect.Lists;
import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.ConfirmationLinkLogic;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.ChallengeDAO;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB;
import com.kameo.challenger.domain.challenges.db.ChallengeStatus;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.mail.MailService;
import com.kameo.challenger.utils.StringHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.util.List;

//@AutoConfigureDataJpa
@TestPropertySource(locations="classpath:application-test.properties")
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class InvitationsTest implements En {

    @Inject
    private AnyDAO anyDao;
    @Inject
    private ChallengerLogic challengerService;
    @Inject
    private ChallengeDAO challengerDao;
    @Inject
    private TestHelper testHelper;
    @Inject
    private ConfirmationLinkLogic confirmationLinkLogic;

    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }


    public InvitationsTest() {
        When("^I invite my friend to new challenge$", () -> {

            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();

            ChallengeODB cb = new ChallengeODB();

            ChallengeParticipantODB cpa1=new ChallengeParticipantODB();
            cpa1.setChallenge(cb);
            cpa1.setUser(myself);
            ChallengeParticipantODB cpa2=new ChallengeParticipantODB();
            cpa2.setChallenge(cb);
            cpa2.setUser(myFriend);
            cb.setParticipants(Lists.newArrayList(cpa1, cpa2));



            challengerDao.createNewChallenge(myself.getId(), cb);
        });


        Then("^my friend should see (\\d+) unanswered challenge invitation$", (Integer arg2) -> {
            UserODB myFriend = testHelper.myFriend();
            List<ChallengeODB> pending = challengerService.getPendingChallenges(myFriend.getId());
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
            testHelper.createUsers("myFriend", "myFriend1", "myFriend2", "myFriend3");
            List<String> logins = challengerService.findUsersWithLoginsStartingWith("myFriend");
            Assert.assertEquals(4, logins.size());
        });

    }

    private void inviteNonExistingUserByEmail() {

        When("^I invite non existing email contact to new challenge$", () -> {
            String friendEmail = "myFriend@email.em";
            UserODB myself = testHelper.myself();

            ChallengeODB cb = new ChallengeODB();
            ChallengeParticipantODB cpa1=new ChallengeParticipantODB();
            cpa1.setChallenge(cb);
            cpa1.setUser(myself);
            ChallengeParticipantODB cpa2=new ChallengeParticipantODB();
            cpa2.setChallenge(cb);
            cpa2.setUser(UserODB.ofEmail(friendEmail));
            cb.setParticipants(Lists.newArrayList(cpa1, cpa2));


            challengerDao.createNewChallenge(myself.getId(), cb);
        });

        When("^I invite existing email contact to new challenge$", () -> {
            testHelper.createUsers("myFriend");
            String friendEmail = "myFriend@email.em";
            UserODB myself = testHelper.myself();

            ChallengeODB cb = new ChallengeODB();
            ChallengeParticipantODB cpa1=new ChallengeParticipantODB();
            cpa1.setChallenge(cb);
            cpa1.setUser(myself);
            ChallengeParticipantODB cpa2=new ChallengeParticipantODB();
            cpa2.setChallenge(cb);
            cpa2.setUser(UserODB.ofEmail(friendEmail));
            cb.setParticipants(Lists.newArrayList(cpa1, cpa2));

            challengerDao.createNewChallenge(myself.getId(), cb);
        });

        Then("^He gets email notification$", () -> {
            Assert.assertEquals(1, testHelper.getSentMessagesList().size());
            testHelper.getSentMessagesList().clear();
        });
    }

    private void nonExistingUserConfirmsEmailInvitation() {
        Given("^I am not challenger user$", () ->
                Assert.assertFalse(testHelper.myselfOptional().isPresent())
        );

        Given("^I received email invitation from my friend$", () -> {
            testHelper.createUsers("myFriend");
            UserODB myFriend = anyDao.getOnlyOne(UserODB.class, u -> "myFriend".equals(u.getLogin()));
            ChallengeODB cb = new ChallengeODB();
            ChallengeParticipantODB cpa1=new ChallengeParticipantODB();
            cpa1.setChallenge(cb);
            cpa1.setUser(myFriend);
            ChallengeParticipantODB cpa2=new ChallengeParticipantODB();
            cpa2.setChallenge(cb);
            cpa2.setUser(UserODB.ofEmail("myself@email.em"));
            cb.setParticipants(Lists.newArrayList(cpa1, cpa2));

            challengerDao.createNewChallenge(myFriend.getId(), cb);
        });

        When("^I accept email link$", () -> {
            Assert.assertEquals(1, testHelper.getSentMessagesList().size());
            MailService.Message mm = testHelper.getSentMessagesList().get(0);
            testHelper.getSentMessagesList().clear();
            String actionUrl = StringHelper.getFirstHrefValue(mm.getContent());
            int i = actionUrl.lastIndexOf("/");
            String uid = actionUrl.substring(i + 1);
            if (confirmationLinkLogic.isConfirmationLinkRequireParams(uid)) {
                confirmationLinkLogic.fillLoginAndPasswordToConfirmationLink(uid, "myself", "myselfpass");
            }
            confirmationLinkLogic.confirmLinkByUid(uid);

        });

        Then("^my account will be created$", () ->
                testHelper.myself()
        );

        Then("^my friend challenge will be accepted$", () -> {

            UserODB myFriend = testHelper.myFriend();

            anyDao.getOnlyOne(ChallengeParticipantODB.class, cc ->
                    cc.getUser().equals(myFriend)
                            && cc.getChallengeStatus() == ChallengeStatus.ACTIVE
            );


        });
    }
}
