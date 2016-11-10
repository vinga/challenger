package com.kameo.challenger;


import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.tasks.TaskDAO;
import com.kameo.challenger.domain.tasks.db.TaskODB;
import com.kameo.challenger.domain.tasks.db.TaskProgressODB;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//@AutoConfigureDataJpa
@TestPropertySource(locations="classpath:application-test.properties")
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class TaskProgressTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private TaskDAO taskDao;
    @Inject
    private ChallengerLogic challengerService;
    @Inject
    private TestHelper testHelper;

    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }


    public TaskProgressTest() {


        Then("^\"([^\"]*)\" see? task \"([^\"]*)\" as \"(done|undone)\"$", (String login, String task, String doneUndone) -> {
            // Write code here that turns the phrase above into concrete actions
            UserODB user1 = testHelper.resolveUserByLogin(login);
            TaskODB taskODB = testHelper.resolveTask(task);
            List<TaskODB> res = challengerService
                    .getTasks(user1.getId(), taskODB.getChallenge().getId(), new Date())
                    .stream().filter(t->t.getUser().getId()==user1.getId()).collect(Collectors.toList());
            List<TaskProgressODB> taskProgress = taskDao.getTaskProgress(res, new Date());

            if (doneUndone.equals("undone")) {
                taskProgress.forEach(tp -> {
                    if (tp.getTask().getId()==taskODB.getId()) {
                        Assert.assertEquals(false,tp.getDone());
                    }
                });
            } else if (doneUndone.equals("done")) {
                TaskProgressODB tpODB = taskProgress.stream()
                                                              .filter(tp -> tp.getTask().getId() == taskODB.getId())
                                                              .findAny().orElse(null);
                Assert.assertEquals(true,tpODB.getDone());
            } else throw new IllegalArgumentException(doneUndone +" should be done or undone");

        });

        When("^\"([^\"]*)\" mark task \"([^\"]*)\" as \"(done|undone)\"$", (String login, String task, String doneUndone) -> {
            UserODB user1 = testHelper.resolveUserByLogin(login);
            TaskODB taskODB = testHelper.resolveTask(task);
            boolean done=false;
            switch (doneUndone) {
                case "undone":
                    done = false;
                    break;
                case "done":
                    done = true;
                    break;
                default:
                    throw new IllegalArgumentException(doneUndone + " should be done or undone");
            }
            try {
                taskDao.markTaskDone(user1.getId(), taskODB.getId(),new Date(), done);
            } catch (Exception ex) {
                testHelper.pushException(ex);
            }
        });

        Then("^I get an exception$", () -> testHelper.popException());

        Then("^I don't get an exception$", () -> {
            try {
                testHelper.popException();
                Assert.fail();
            } catch (Exception ex) {
               // no exception should exist, so popException should throw exception ifnorming about that
            }
        });


    }


}
