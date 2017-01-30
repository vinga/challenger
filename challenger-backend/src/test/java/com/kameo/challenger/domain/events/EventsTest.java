package com.kameo.challenger.domain.events;

import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.events.db.EventODB;
import com.kameo.challenger.domain.events.db.EventReadODB;
import com.kameo.challenger.domain.events.db.EventType;
import com.kameo.challenger.domain.tasks.db.TaskODB;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import kotlin.Pair;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class EventsTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private TestHelper testHelper;
    @Inject
    private EventGroupDAO eventGroupDao;

    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }

    public EventsTest() {
        Given("^\"([^\"]*)\" commented action \"([^\"]*)\" with words \"([^\"]*)\"$", (String login, String task, String words) -> {
            UserODB u = testHelper.resolveUserByLogin(login);
            TaskODB t = testHelper.resolveTask(task);
            EventODB p = new EventODB();
            p.setAuthor(u);
            p.setChallenge(t.getChallenge());
            p.setCreateDate(new Date());
            p.setContent(words);
            p.setEventType(EventType.POST);
            eventGroupDao.createEventFromClient(u.getId(), t.getChallenge().getId(), p);
        });
        When("^\"([^\"]*)\" fetch all posts for action \"([^\"]*)\"$", (String login, String task) -> {
            UserODB u = testHelper.resolveUserByLogin(login);
            TaskODB t = testHelper.resolveTask(task);
            eventGroupDao.getEventsForTask(u.getId(), t.getChallenge().getId(), t.getId());
        });
        Then("^\"([^\"]*)\" see that action \"([^\"]*)\" has (\\d+) post$", (String login, String task, Integer taskNo) -> {
            UserODB u = testHelper.resolveUserByLogin(login);
            TaskODB t = testHelper.resolveTask(task);
            List<EventODB> res = eventGroupDao.getEventsForTask(u.getId(), t.getChallenge().getId(), t.getId());
            Assert.assertEquals(taskNo.intValue(), res.size());
        });
        Given("^\"([^\"]*)\" commented action \"([^\"]*)\" with words \"([^\"]*)\" (\\d+) times$", (String login, String action, String words, Integer times) -> {
            UserODB u = testHelper.resolveUserByLogin(login);
            TaskODB t = testHelper.resolveTask(action);
            for (int i = 0; i < times; i++) {
                EventODB p = new EventODB();
                p.setAuthor(u);
                p.setChallenge(t.getChallenge());
                p.setCreateDate(new Date());
                p.setContent(words);
                p.setEventType(EventType.POST);
                eventGroupDao.createEventFromClient(u.getId(), p.getChallenge().getId(), p);
            }
        });
        Given("^\"([^\"]*)\" commented challenge \"([^\"]*)\" with words \"([^\"]*)\" (\\d+) times$", (String login, String challenge, String words, Integer times) -> {
            UserODB u = testHelper.resolveUserByLogin(login);
            ChallengeODB c = testHelper.resolveChallenge(challenge);
            for (int i = 0; i < times; i++) {
                EventODB p = new EventODB();
                p.setAuthor(u);
                p.setChallenge(c);
                p.setCreateDate(new Date());
                p.setContent(words);
                p.setEventType(EventType.POST);
                eventGroupDao.createEventFromClient(u.getId(), c.getId(), p);
            }
        });
        Given("^\"([^\"]*)\" read all events in challenge \"([^\"]*)\"$", (String login, String challenge) -> {
            UserODB u = testHelper.resolveUserByLogin(login);
            ChallengeODB c = testHelper.resolveChallenge(challenge);
            long challengeId = c.getId();
            List<EventODB> events = anyDao.streamAll(EventODB.class).where(e -> e.getChallenge().getId() == challengeId)
                                          .sortedBy(EventODB::getId).collect(Collectors.toList());
            for (EventODB e : events) {
                eventGroupDao.markEventAsRead(u.getId(), challengeId, e.getId(), new Date());
            }
        });
        When("^\"([^\"]*)\" fetch (\\d+) total posts for challenge \"([^\"]*)\"$", (String login, Integer postsCount, String challenge) -> {
            UserODB u = testHelper.resolveUserByLogin(login);
            ChallengeODB c = testHelper.resolveChallenge(challenge);
            eventGroupDao.getLastEventsForChallenge(u.getId(), c.getId(), null, postsCount);
        });
        Then("^\"([^\"]*)\" see that last (\\d+) comments of challenge \"([^\"]*)\" contains (\\d+) (?:post|posts) with \"([^\"]*)\"$",
                (String login, Integer commentsCount, String challenge, Integer commentsOfTypeCount, String words) -> {
                    UserODB u = testHelper.resolveUserByLogin(login);
                    ChallengeODB c = testHelper.resolveChallenge(challenge);
                    List<Pair<EventReadODB, EventODB>> pairs = eventGroupDao.getLastEventsForChallenge(u.getId(), c.getId(),null,  commentsCount);
                    List<EventODB> posts = pairs.stream().map(Pair<EventReadODB, EventODB>::getSecond).collect(Collectors.toList());
                    int count = 0;
                    for (EventODB p : posts) {
                        if (p.getContent().equals(words))
                            count++;
                    }
                    Assert.assertEquals("Should be " + commentsOfTypeCount + " for words '" + words + "'", (long) commentsOfTypeCount, (long) count);
                });
    }
}
