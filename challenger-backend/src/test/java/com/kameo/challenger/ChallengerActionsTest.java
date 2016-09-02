package com.kameo.challenger;


import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@AutoConfigureDataJpa
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class ChallengerActionsTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private ChallengerLogic challengerService;
    @Inject
    private TestHelper testHelper;

    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }


    public ChallengerActionsTest() {
        seeingChallengerActionsDefinitions();


        Given("^my friend created new \"([^\"]*)\" action for me$", (String arg1) -> {
            Assert.assertTrue(Sets.newHashSet("onetime", "daily", "monthly", "weekly").contains(arg1));

            ChallengeContractODB cc = testHelper
                    .getActiveContactBetween(testHelper.myself(), testHelper.myFriend());
            ChallengeActionODB ca = new ChallengeActionODB();
            ca.setUser(testHelper.myself());
            ca.setChallengeContract(cc);
            ca.setActionType(ActionType.valueOf(arg1));
            challengerService.createNewChallengeAction(testHelper.myFriend().getId(), ca);
        });

        Given("^my friend created new onetime action for me with due date in the past$", () -> {
            ChallengeContractODB cc = testHelper
                    .getActiveContactBetween(testHelper.myself(), testHelper.myFriend());
            ChallengeActionODB ca = new ChallengeActionODB();

            ca.setUser(testHelper.myself());
            ca.setChallengeContract(cc);
            ca.setDueDate(DateUtils.addDays(new Date(), -1));
            ca.setActionType(ActionType.onetime);
            challengerService.createNewChallengeAction(testHelper.myFriend().getId(), ca);
        });

        When("^I get list of waiting for acceptance actions$", () -> {

        });


        Then("^I should see only (\\d+) waiting for acceptance actions?$", (Integer arg1) -> {
            ChallengeContractODB cc = testHelper
                    .getActiveContactBetween(testHelper.myself(), testHelper.myFriend());

            List<ChallengeActionODB> actions = challengerService
                    .getPendingChallengeActionsForConctract(testHelper.myself().getId(), cc.getId());

            Assert.assertEquals(arg1.longValue(), actions.size());
        });


        When("^I accept all actions before due date$", () -> {
            ChallengeContractODB cc = testHelper
                    .getActiveContactBetween(testHelper.myself(), testHelper.myFriend());
            Assert.assertNotNull(cc);
            List<ChallengeActionODB> collect = anyDao.streamAll(ChallengeActionODB.class).collect(Collectors.toList());
            testHelper.acceptAllChallengeActions(testHelper.myself().getId(), collect);

        });

        Then("^I should see (\\d+) actions on daily-todo-list$", (Integer arg1) -> {
            ChallengeContractODB cc = testHelper
                    .getActiveContactBetween(testHelper.myself(), testHelper.myFriend());
            List<ChallengeActionODB> actions = challengerService
                    .getMyDailyTodoList(testHelper.myself().getId(), cc.getId());
            Assert.assertEquals(arg1.longValue(), actions.size());
        });
    }

    private void seeingChallengerActionsDefinitions() {

        Given("^I have accepted challenge with my friend$", () -> {
            testHelper.createUsers("myself", "myFriend");
            testHelper.createAcceptedChallenge(Iterators.forArray(testHelper.myself(), testHelper.myFriend()));
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();
            ChallengeContractODB cc = anyDao.streamAll(ChallengeContractODB.class)
                                            .where(u -> u.getFirst().equals(myself) && u.getSecond().equals(myFriend))
                                            .getOnlyValue();
            Assert.assertNotNull(cc);
        });

        When("my friend created new action for me$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();
            ChallengeContractODB cc = anyDao.streamAll(ChallengeContractODB.class)
                                            .where(u -> u.getFirst().equals(myself) && u.getSecond().equals(myFriend))
                                            .getOnlyValue();

            ChallengeActionODB ca = new ChallengeActionODB();
            ca.setActionType(ActionType.daily);
            ca.setIcon("fa-car");
            ca.setActionName("Test");
            ca.setActionStatus(ActionStatus.pending);
            ca.setChallengeContract(cc);
            ca.setUser(myself);
            challengerService.createNewChallengeAction(myFriend.getId(), ca);
        });


        Then("^I should see it$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();
            ChallengeContractODB cc = anyDao.streamAll(ChallengeContractODB.class)
                                            .where(u -> u.getFirst().equals(myself) && u.getSecond().equals(myFriend))
                                            .getOnlyValue();

            List<ChallengeActionODB> actions = challengerService.getChallengeActions(myself.getId(), cc.getId());
            Assert.assertFalse(actions.isEmpty());
        });


    }


}
