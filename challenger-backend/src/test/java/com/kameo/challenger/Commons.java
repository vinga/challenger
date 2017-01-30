package com.kameo.challenger;

import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.ChallengeDAO;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.challenges.db.ChallengeStatus;
import com.kameo.challenger.logic.FakeDataLogic;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.util.List;

@TestPropertySource(locations="classpath:application-test.properties")
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class Commons implements En {

    @Inject
    private AnyDAO anyDao;
    @Inject
    private ChallengeDAO challengeDAO;
    @Inject
    private FakeDataLogic cmd;
    @Inject
    private TestHelper testHelper;


    public Commons() {
        Given("^I am existing challenger user$", () -> cmd.createUsers("myself"));
        Given("^my friend is existing challenger user$", () ->
                cmd.createUsers("myFriend")
        );

        Given("^\"([^\"]*)\" (?:have|has) accepted challenge \"([^\"]*)\" with \"([^\"]*)\"$", (String person1, String challengeName, String person2) -> {
            List<UserODB> users = testHelper.createUsers(testHelper.resolveLogins(person1, person2));
            testHelper.createAcceptedChallengeWithLabel(challengeName, users.iterator());
        });


        // challenge must exists
        When("^\"([^\"]*)\" accepted challenge \"([^\"]*)\"$", (String person1, String challengeName) -> {
            List<UserODB> users = testHelper.createUsers(testHelper.resolveLogins(person1));
            ChallengeODB challenge = testHelper.resolveChallenge(challengeName);
            challengeDAO.updateChallengeState(users.get(0).getId(), challenge.getId(), ChallengeStatus.ACTIVE);

        });

        Then("^challenge \"([^\"]*)\" has status \"([^\"]*)\"$", (String arg1, String arg2) -> {
            ChallengeStatus stat = null;
            switch (arg2) {
                case "active":
                    stat = ChallengeStatus.ACTIVE;
                    break;
                case "refused":
                    stat = ChallengeStatus.REFUSED;
                    break;
                case "waiting for acceptance":
                    stat = ChallengeStatus.WAITING_FOR_ACCEPTANCE;
                    break;
            }
            Assert.assertEquals(testHelper.resolveChallenge(arg1).getChallengeStatus(), stat);

        });

    }

}
