package com.kameo.challenger;


import com.google.common.collect.Sets;
import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.ChallengeDAO;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.tasks.TaskDAO;
import com.kameo.challenger.domain.tasks.db.TaskODB;
import com.kameo.challenger.domain.tasks.db.TaskODB.TaskDifficulty;
import com.kameo.challenger.domain.tasks.db.TaskStatus;
import com.kameo.challenger.domain.tasks.db.TaskType;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//@AutoConfigureDataJpa
@TestPropertySource(locations="classpath:application-test.properties")
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class TasksTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private TaskDAO taskDao;
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


    public TasksTest() {
        seeingChallengerActionsDefinitions();

        Given("^\"([^\"]*)\" marked task \"([^\"]*)\" as done every day$",
                (String userLogin, String taskName) -> {
                    UserODB u=testHelper.resolveUserByLogin(userLogin);
                    TaskODB t=testHelper.resolveTask(taskName);

                    LocalDateTime ld=t.getCreateDate();
                    while (ld.isBefore(LocalDateTime.now())) {
                        taskDao.markTaskDone(u.getId(),t.getChallenge().getId(), t.getId(),ld.toLocalDate(),true);
                        ld=ld.plusDays(1);
                    }

                });
        Given("^\"([^\"]*)\" marked task \"([^\"]*)\" as done for (\\d+) last days$",
                (String userLogin, String taskName, Integer days) -> {
                    UserODB u=testHelper.resolveUserByLogin(userLogin);
                    TaskODB t=testHelper.resolveTask(taskName);

                    int d=days;
                    LocalDate ld=LocalDate.now();
                    while (d-->0) {
                        taskDao.markTaskDone(u.getId(), t.getChallenge().getId(), t.getId(),ld,true);
                        ld=ld.minusDays(1);
                    }

                });


        Given("^\"([^\"]*)\" created in challenge \"([^\"]*)\" new task \"([^\"]*)\" for \"([^\"]*)\" (\\d+) days ago$",
                (String person1, String challengeContractName, String actionName, String person2, Integer daysAgo) -> {
            UserODB u1=testHelper.resolveUserByLogin(person1);
            UserODB u2=testHelper.resolveUserByLogin(person2);
            ChallengeODB cc = testHelper.resolveChallenge(challengeContractName);
            TaskODB ca = new TaskODB();
                    ca.setCreateDate(LocalDateTime.now().minusDays(daysAgo));
            ca.setTaskType(TaskType.daily);
            ca.setIcon("fa-car");
            ca.setLabel(actionName);
            ca.setChallenge(cc);
            ca.setUser(u2);
            taskDao.createTask(Sets.newHashSet(u1.getId()), ca);
        });
        Given("^\"([^\"]*)\" created in challenge \"([^\"]*)\" new difficult task \"([^\"]*)\" for \"([^\"]*)\" (\\d+) days ago$",
                (String person1, String challengeContractName, String actionName, String person2, Integer daysAgo) -> {
                    UserODB u1=testHelper.resolveUserByLogin(person1);
                    UserODB u2=testHelper.resolveUserByLogin(person2);
                    ChallengeODB cc = testHelper.resolveChallenge(challengeContractName);
                    TaskODB ca = new TaskODB();
                    ca.setCreateDate(LocalDateTime.now().minusDays(daysAgo));
                    ca.setDifficulty(TaskDifficulty.HARD.ordinal());
                    ca.setTaskType(TaskType.daily);
                    ca.setIcon("fa-car");
                    ca.setLabel(actionName);
                    ca.setChallenge(cc);
                    ca.setUser(u2);
                    taskDao.createTask(Sets.newHashSet(u1.getId()), ca);
                });

        Given("^\"([^\"]*)\" accepted task \"([^\"]*)\" in challenge \"([^\"]*)\"$",
                (String person1, String taskName, String challengeName) -> {
                    final UserODB userODB = testHelper.resolveUserByLogin(person1);
                    final ChallengeODB challenge = testHelper.resolveChallenge(challengeName);
                    final TaskODB taskODB = testHelper.resolveTask(taskName);
                    taskDao.changeTaskStatus(challenge.getId(), taskODB.getId(), Sets.newHashSet(userODB.getId()), TaskStatus.accepted, null);
                });

        Given("^my friend created new \"([^\"]*)\" action for me$", (String arg1) -> {
            Assert.assertTrue(Sets.newHashSet("onetime", "daily", "monthly", "weekly").contains(arg1));

            ChallengeODB cc = testHelper
                    .getActiveChallengeBetween(testHelper.myself(), testHelper.myFriend());
            TaskODB ca = new TaskODB();
            ca.setUser(testHelper.myself());
            ca.setChallenge(cc);
            ca.setTaskType(TaskType.valueOf(arg1));
            taskDao.createTask(Sets.newHashSet(testHelper.myFriend().getId()), ca);
        });

        Given("^my friend created new onetime action for me with due date in the past$", () -> {
            ChallengeODB cc = testHelper
                    .getActiveChallengeBetween(testHelper.myself(), testHelper.myFriend());
            TaskODB ca = new TaskODB();

            ca.setUser(testHelper.myself());
            ca.setChallenge(cc);
            ca.setDueDate(LocalDateTime.now().minusDays(1));
            ca.setTaskType(TaskType.onetime);
            taskDao.createTask(Sets.newHashSet(testHelper.myFriend().getId()), ca);
        });

        When("^I get list of waiting for acceptance actions$", () -> {

        });


        Then("^I should see only (\\d+) waiting for acceptance actions?$", (Integer arg1) -> {
            ChallengeODB cc = testHelper
                    .getActiveChallengeBetween(testHelper.myself(), testHelper.myFriend());

            List<TaskODB> actions = challengerService
                    .getWaitingForAcceptanceTasksForChallenge(testHelper.myself().getId(), cc.getId());

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
                    .getTasks(testHelper.myself().getId(), cc.getId(), LocalDate.now())
                    .stream().filter(t->t.getUser().getId()==testHelper.myself().getId()).collect(Collectors.toList());
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
            taskDao.createTask(Sets.newHashSet(u1.getId()), ca);
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
            ChallengeODB cc = testHelper.getChallengeBetween(myself, myFriend);

            TaskODB ca = new TaskODB();
            ca.setTaskType(TaskType.daily);
            ca.setIcon("fa-car");
            ca.setLabel("Test");
            ca.setTaskStatus(TaskStatus.accepted);
            ca.setChallenge(cc);
            ca.setUser(myself);
            taskDao.createTask(Sets.newHashSet(myFriend.getId()), ca);
        });


        Then("^I should see mine actions$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();

            ChallengeODB cc = testHelper.getChallengeBetween(myself, myFriend);

            List<TaskODB> actions = challengerService.getTasks(myself.getId(), cc.getId(), LocalDate.now())
                    .stream().filter(t->t.getUser().getId()==myself.getId()).collect(Collectors.toList());
            Assert.assertFalse(actions.isEmpty());
        });
        Then("^I should see my friend's actions$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();
            ChallengeODB cc = testHelper.getChallengeBetween(myself, myFriend);

            List<TaskODB> actions = challengerService.getTasks(myself.getId(), cc.getId(),LocalDate.now())
                    .stream().filter(t->t.getUser().getId()==myFriend.getId()).collect(Collectors.toList());
            Assert.assertFalse(actions.isEmpty());
        });

        When("^my friend created new action for him$", () -> {
            UserODB myself = testHelper.myself();
            UserODB myFriend = testHelper.myFriend();
            ChallengeODB cc = testHelper.getChallengeBetween(myself, myFriend);

            TaskODB ca = new TaskODB();
            ca.setTaskType(TaskType.daily);
            ca.setIcon("fa-car");
            ca.setLabel("Test");
            ca.setTaskStatus(TaskStatus.accepted);
            ca.setChallenge(cc);
            ca.setUser(myFriend);
            taskDao.createTask(Sets.newHashSet(myFriend.getId()), ca);
        });




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
            challengerService.getTasks(user1.getId(),challengeODB.getId(), LocalDate.now());
        });

        Then("^\"([^\"]*)\" last visible challenge is \"([^\"]*)\"$", (String arg1, String arg2) -> {
            UserODB user1 = testHelper.resolveUserByLogin(arg1);
            ChallengeODB expected = testHelper.resolveChallenge(arg2);
            ChallengeODB actual = anyDao
                    .get(ChallengeODB.class, challengerDao.getVisibleChallenges(user1.getId()).getDefaultChallengeId());
            Assert.assertEquals(expected.getLabel(),actual.getLabel());
        });

    }


}
