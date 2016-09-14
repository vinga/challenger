package com.kameo.challenger;


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
public class TasksTest implements En {
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


    public TasksTest() {
        seeingChallengerActionsDefinitions();


        Given("^my friend created new \"([^\"]*)\" action for me$", (String arg1) -> {
            Assert.assertTrue(Sets.newHashSet("onetime", "daily", "monthly", "weekly").contains(arg1));

            ChallengeODB cc = testHelper
                    .getActiveChallengeBetween(testHelper.myself(), testHelper.myFriend());
            TaskODB ca = new TaskODB();
            ca.setUser(testHelper.myself());
            ca.setChallenge(cc);
            ca.setTaskType(TaskType.valueOf(arg1));
            challengerService.updateTask(testHelper.myFriend().getId(), ca);
        });

        Given("^my friend created new onetime action for me with due date in the past$", () -> {
            ChallengeODB cc = testHelper
                    .getActiveChallengeBetween(testHelper.myself(), testHelper.myFriend());
            TaskODB ca = new TaskODB();

            ca.setUser(testHelper.myself());
            ca.setChallenge(cc);
            ca.setDueDate(DateUtils.addDays(new Date(), -1));
            ca.setTaskType(TaskType.onetime);
            challengerService.updateTask(testHelper.myFriend().getId(), ca);
        });

        When("^I get list of waiting for acceptance actions$", () -> {

        });


        Then("^I should see only (\\d+) waiting for acceptance actions?$", (Integer arg1) -> {
            ChallengeODB cc = testHelper
                    .getActiveChallengeBetween(testHelper.myself(), testHelper.myFriend());

            List<TaskODB> actions = challengerService
                    .getWaitingForAcceptanceTasksForConctract(testHelper.myself().getId(), cc.getId());

            Assert.assertEquals(arg1.longValue(), actions.size());
        });


        When("^I accept all actions before due date$", () -> {
            ChallengeODB cc = testHelper
                    .getActiveChallengeBetween(testHelper.myself(), testHelper.myFriend());
            Assert.assertNotNull(cc);
            List<TaskODB> collect = anyDao.streamAll(TaskODB.class).collect(Collectors.toList());
            testHelper.acceptAllTasks(testHelper.myself().getId(), collect);

        });

        Then("^I should see (\\d+) actions on daily-todo-list$", (Integer arg1) -> {
            ChallengeODB cc = testHelper
                    .getActiveChallengeBetween(testHelper.myself(), testHelper.myFriend());
            List<TaskODB> actions = challengerService
                    .getTasksAssignedToPerson(testHelper.myself().getId(), testHelper.myself().getId(), cc.getId(), new Date());
            Assert.assertEquals(arg1.longValue(), actions.size());
        });
    }

    private void seeingChallengerActionsDefinitions() {
        // Given "I" created in challenge "boom" new action "testAction" for "me"
        Given("^\"([^\"]*)\" created in challenge \"([^\"]*)\" new action \"([^\"]*)\" for \"([^\"]*)\"", (String person1, String challengeContractName, String actionName, String person2) -> {
            UserODB u1=testHelper.resolveUserByLogin(person1);
            UserODB u2=testHelper.resolveUserByLogin(person2);
            ChallengeODB cc = testHelper.resolveChallenge(challengeContractName);
            TaskODB ca = new TaskODB();
            ca.setTaskType(TaskType.daily);
            ca.setIcon("fa-car");
            ca.setLabel(actionName);
            ca.setChallenge(cc);
            ca.setUser(u2);
            challengerService.updateTask(u1.getId(), ca);
        });

        Then("^\"([^\"]*)\" can modify action \"([^\"]*)\"$", (String person, String arg1) -> {
            UserODB user = testHelper.resolveUserByLogin(person);
            try {
                TaskODB caa = anyDao.getOnlyOne(TaskODB.class, ca -> ca.getLabel().equals(arg1));
                challengerService.updateTask(user.getId(), caa);
            } catch (Exception ex) {
                Assert.fail();
            }
        });
        Then("^\"([^\"]*)\" cannot modify action \"([^\"]*)\"$", (String person, String arg1) -> {
            UserODB user = testHelper.resolveUserByLogin(person);
            try {
                TaskODB caa = anyDao.getOnlyOne(TaskODB.class, ca -> ca.getLabel().equals(arg1));
                challengerService.updateTask(user.getId(), caa);
                Assert.fail();
            } catch (Exception ex) {
                //
            }
        });

        When("my friend created new action for me$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();
            ChallengeODB cc = anyDao.streamAll(ChallengeODB.class)
                                            .where(u -> u.getFirst().equals(myself) && u.getSecond().equals(myFriend))
                                            .getOnlyValue();

            TaskODB ca = new TaskODB();
            ca.setTaskType(TaskType.daily);
            ca.setIcon("fa-car");
            ca.setLabel("Test");
            ca.setTaskStatus(TaskStatus.accepted);
            ca.setChallenge(cc);
            ca.setUser(myself);
            challengerService.updateTask(myFriend.getId(), ca);
        });


        Then("^I should see mine actions$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();
            ChallengeODB cc = anyDao.streamAll(ChallengeODB.class)
                                            .where(u -> u.getFirst().equals(myself) && u.getSecond().equals(myFriend))
                                            .getOnlyValue();

            List<TaskODB> actions = challengerService.getTasksAssignedToPerson(myself.getId(), myself.getId(), cc.getId(), new Date());
            Assert.assertFalse(actions.isEmpty());
        });
        Then("^I should see my friend's actions$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();
            ChallengeODB cc = anyDao.streamAll(ChallengeODB.class)
                                            .where(u -> u.getFirst().equals(myself) && u.getSecond().equals(myFriend))
                                            .getOnlyValue();

            List<TaskODB> actions = challengerService.getTasksAssignedToPerson(myself.getId(), myFriend.getId(), cc.getId(), new Date());
            Assert.assertFalse(actions.isEmpty());
        });

        When("^my friend created new action for him$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();
            ChallengeODB cc = anyDao.streamAll(ChallengeODB.class)
                                            .where(u -> u.getFirst().equals(myself) && u.getSecond().equals(myFriend))
                                            .getOnlyValue();

            TaskODB ca = new TaskODB();
            ca.setTaskType(TaskType.daily);
            ca.setIcon("fa-car");
            ca.setLabel("Test");
            ca.setTaskStatus(TaskStatus.accepted);
            ca.setChallenge(cc);
            ca.setUser(myFriend);
            challengerService.updateTask(myFriend.getId(), ca);
        });




    /*    When("^my friend accepted action \"([^\"]*)\"$", (String arg1) -> {
            UserODB myFriend = testHelper.myFriend();
            TaskODB caa = anyDao.getOnlyOne(TaskODB.class, ca -> ca.getLabel().equals(arg1));
            Assert.assertEquals(TaskStatus.waiting_for_acceptance, caa.getTaskStatus());
            caa.setTaskStatus(TaskStatus.pending);
            challengerService.updateTask(myFriend.getId(),caa);
        });*/



        Then("^action \"([^\"]*)\" (isn't|is not|is) accepted$", (String arg1, String cond) -> {
            TaskODB ca=testHelper.resolveTask(arg1);
            if (cond.equals("is"))
                 Assert.assertNotEquals(TaskStatus.waiting_for_acceptance,ca.getTaskStatus());
            else
                Assert.assertEquals(TaskStatus.waiting_for_acceptance,ca.getTaskStatus());
        });




        When("^\"([^\"]*)\" view todo list of challenge \"([^\"]*)\"$", (String arg1, String arg2) -> {
            UserODB user1 = testHelper.resolveUserByLogin(arg1);
            ChallengeODB challengeODB = testHelper.resolveChallenge(arg2);
            challengerService.getTasksAssignedToPerson(user1.getId(),user1.getId(),challengeODB.getId(), new Date());
        });

        Then("^\"([^\"]*)\" last visible challenge is \"([^\"]*)\"$", (String arg1, String arg2) -> {
            UserODB user1 = testHelper.resolveUserByLogin(arg1);
            ChallengeODB expected = testHelper.resolveChallenge(arg2);
            ChallengeODB actual = anyDao
                    .get(ChallengeODB.class, challengerService.getVisibleChallenges(user1.getId()).getDefaultChallengeId());
            Assert.assertEquals(expected.getLabel(),actual.getLabel());
        });

    }


}
