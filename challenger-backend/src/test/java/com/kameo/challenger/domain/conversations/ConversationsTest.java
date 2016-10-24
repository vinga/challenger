package com.kameo.challenger.domain.conversations;

import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.ConfirmationLinkLogic;
import com.kameo.challenger.domain.accounts.EventGroupDAO;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.events.EventODB;
import com.kameo.challenger.domain.tasks.db.TaskODB;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class ConversationsTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private TestHelper testHelper;
    @Inject
    private ConfirmationLinkLogic confirmationLinkLogic;
    @Inject
    private EventGroupDAO eventGroupDao;

    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }


    public ConversationsTest() {

        Given("^\"([^\"]*)\" commented action \"([^\"]*)\" with words \"([^\"]*)\"$", (String login, String task, String words) -> {
            UserODB u=testHelper.resolveUserByLogin(login);
            TaskODB t=testHelper.resolveTask(task);
            EventODB p=new EventODB();
            p.setAuthor(u);
            p.setTask(t);
            p.setChallenge(t.getChallenge());
            p.setCreateDate(new Date());
            p.setContent(words);
            eventGroupDao.editPost(u.getId(),p);

        });

        When("^\"([^\"]*)\" fetch all posts for action \"([^\"]*)\"$", (String login, String task) -> {
            UserODB u=testHelper.resolveUserByLogin(login);
            TaskODB t=testHelper.resolveTask(task);
            eventGroupDao.getPostsForTask(u.getId(),t.getId());

        });

        Then("^\"([^\"]*)\" see that action \"([^\"]*)\" has (\\d+) post$", (String login,String task, Integer taskNo) -> {
            UserODB u=testHelper.resolveUserByLogin(login);
            TaskODB t=testHelper.resolveTask(task);
            List<EventODB> res = eventGroupDao.getPostsForTask(u.getId(), t.getId());
            Assert.assertEquals(taskNo.intValue(),res.size());
        });



        Given("^\"([^\"]*)\" commented action \"([^\"]*)\" with words \"([^\"]*)\" (\\d+) times$", (String login, String action, String words, Integer times) -> {
            UserODB u=testHelper.resolveUserByLogin(login);
            TaskODB t=testHelper.resolveTask(action);
            for (int i=0; i<times; i++) {
                EventODB p = new EventODB();
                p.setAuthor(u);
                p.setTask(t);
                p.setChallenge(t.getChallenge());
                p.setCreateDate(new Date());
                p.setContent(words);
                eventGroupDao.editPost(u.getId(), p);
            }
        });

        Given("^\"([^\"]*)\" commented challenge \"([^\"]*)\" with words \"([^\"]*)\" (\\d+) times$", (String login, String challenge, String words, Integer times) -> {
            UserODB u=testHelper.resolveUserByLogin(login);
            ChallengeODB c=testHelper.resolveChallenge(challenge);
            for (int i=0; i<times; i++) {
                EventODB p = new EventODB();
                p.setAuthor(u);
                p.setChallenge(c);
                p.setCreateDate(new Date());
                p.setContent(words);
                eventGroupDao.editPost(u.getId(), p);
            }
        });

        When("^\"([^\"]*)\" fetch (\\d+) total posts for challenge \"([^\"]*)\"$", (String login, Integer postsCount, String challenge) -> {
            UserODB u=testHelper.resolveUserByLogin(login);
            ChallengeODB c=testHelper.resolveChallenge(challenge);
            eventGroupDao.getPostsForChallenge(u.getId(),c.getId(), postsCount);
        });

        Then("^\"([^\"]*)\" see that last (\\d+) comments of challenge \"([^\"]*)\" contains (\\d+) (?:post|posts) with \"([^\"]*)\"$",
                (String login, Integer commentsCount, String challenge, Integer commentsOfTypeCount, String words) -> {
                    UserODB u=testHelper.resolveUserByLogin(login);
                    ChallengeODB c=testHelper.resolveChallenge(challenge);
                    List<EventODB> posts = eventGroupDao.getPostsForChallenge(u.getId(), c.getId(), commentsCount);
                    int count=0;
                    for (EventODB p: posts) {
                        if (p.getContent().equals(words))
                            count++;
                    }
                    Assert.assertEquals("Should be "+commentsOfTypeCount+" for words '"+words+"'",(long)commentsOfTypeCount,(long)count);
                });


    }

}
