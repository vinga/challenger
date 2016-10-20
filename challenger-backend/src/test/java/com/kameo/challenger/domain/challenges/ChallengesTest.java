package com.kameo.challenger.domain.challenges;


import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB;
import com.kameo.challenger.domain.challenges.db.ChallengeStatus;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

//@AutoConfigureDataJpa
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class ChallengesTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private ChallengerLogic challengerService;
    @Inject
    private ChallengeDAO challengerDao;
    @Inject
    private TestHelper testHelper;


    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }


    public ChallengesTest() {

        Given("^I have (\\d+) accepted challenge? with my friend$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            for (int i = 0; i < arg1; i++)
                testHelper.createAcceptedChallenge(users.iterator());
        });

        Given("^I have (\\d+) challenge? sent by me to my friend waiting for acceptance$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            for (int i = 0; i < arg1; i++)
                testHelper.createPendingChallenge(users.iterator());
        });


        Given("^I have (\\d+) challenge waiting for my acceptance$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            Collections.reverse(users);
            for (int i = 0; i < arg1; i++)
                testHelper.createPendingChallenge(users.iterator());
        });

        Given("^I have (\\d+) challenge rejected by me$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            Collections.reverse(users);
            for (int i = 0; i < arg1; i++)
                testHelper.createRejectedChallenge(users.iterator());
        });

        Given("^I have (\\d+) challenge sent by me and rejected by my friend$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            for (int i = 0; i < arg1; i++)
                testHelper.createRejectedChallenge(users.iterator());
        });


        Then("^I should see (\\d+) challenges on my list$", (Integer arg1) -> {
            UserODB myself = testHelper.myself();
            ChallengeDAO.ChallengeInfoDTO res = challengerDao
                    .getVisibleChallenges(myself.getId());

            Assert.assertEquals(arg1.longValue(), res.getVisibleChallenges().size());

        });

        Then("^see which challenge was recently chosen from the list$", () -> {
            UserODB myself = testHelper.myself();
            ChallengeDAO.ChallengeInfoDTO res = challengerDao
                    .getVisibleChallenges(myself.getId());
            Assert.assertNotNull(res.getDefaultChallengeId());
        });


        Given("^\"([^\"]*)\" created challenge \"([^\"]*)\" with \"([^\"]*)\" and \"([^\"]*)\"$", (String u1, String challenge, String u2, String u3) -> {
            // Write code here that turns the phrase above into concrete actions
            List<UserODB> users = testHelper.createUsers(u1, u2, u3);
            UserODB uu1 = users.get(0);
            ChallengeODB ch = testHelper.createPendingChallengeWithLabel(challenge, users.iterator());
            Assert.assertEquals(3, ch.getParticipants().size());

        });


        Then("^challenge \"([^\"]*)\" is waiting for acceptance of \"([^\"]*)\"$", (String ch, String u2) -> {
            UserODB user2 = testHelper.resolveUserByLogin(u2);


            ChallengeODB chall = testHelper.resolveChallenge(ch);


            long chalId = chall.getId();

            Assert.assertTrue(challengerService.getPendingChallenges(user2.getId()).stream().anyMatch(cha -> ch.equals(cha.getLabel())));

            long user2Id = user2.getId();
            Assert.assertEquals(ChallengeStatus.WAITING_FOR_ACCEPTANCE, chall.getChallengeStatus());
            anyDao.streamAll(ChallengeParticipantODB.class)
                    .where(cp -> cp.getChallenge().getId() == chalId).forEach(cp -> {
                if (cp.getUser().getId() == user2Id)
                    Assert.assertEquals(ChallengeStatus.WAITING_FOR_ACCEPTANCE, cp.getChallengeStatus());
                else Assert.assertEquals(ChallengeStatus.ACTIVE, cp.getChallengeStatus());
            });
        });

    }


}
