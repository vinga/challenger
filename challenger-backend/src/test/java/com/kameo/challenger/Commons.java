package com.kameo.challenger;

import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.logic.FakeDataLogic;
import com.kameo.challenger.odb.ChallengeODB;
import com.kameo.challenger.odb.ChallengeStatus;
import com.kameo.challenger.odb.UserODB;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by kmyczkowska on 2016-09-02.
 */
@AutoConfigureDataJpa
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class Commons implements En {

    @Inject
    private AnyDAO anyDao;
    @Inject
    private ChallengerLogic challengerService;
    @Inject
    private FakeDataLogic cmd;
    @Inject
    private TestHelper testHelper;


    public Commons() {
        Given("^I am existing challenger user$", () -> {
            cmd.createUsers("myself");
        });
        Given("^my friend is existing challenger user$", () -> {
            cmd.createUsers("myFriend");
        });

        Given("^\"([^\"]*)\" (?:have|has) accepted challenge \"([^\"]*)\" with \"([^\"]*)\"$", (String person1, String challengeName, String person2) -> {
            List<UserODB> users = testHelper.createUsers(testHelper.resolveLogins(person1, person2));
            testHelper.createAcceptedChallengeWithLabel(challengeName, users.iterator());
        });


        // challenge must exists
        When("^\"([^\"]*)\" accepted challenge \"([^\"]*)\"$", (String person1, String challengeName) -> {
            List<UserODB> users = testHelper.createUsers(testHelper.resolveLogins(person1));
            ChallengeODB challenge = testHelper.resolveChallenge(challengeName);
            challengerService.updateChallengeState(users.get(0).getId(), challenge.getId(), ChallengeStatus.ACTIVE);

        });

        Then("^challenge \"([^\"]*)\" has status \"([^\"]*)\"$", (String arg1, String arg2) -> {
            ChallengeStatus stat=null;
            if (arg2.equals("active"))
                stat=ChallengeStatus.ACTIVE;
            else  if (arg2.equals("refused"))
                stat=ChallengeStatus.REFUSED;
            else  if (arg2.equals("waiting for acceptance"))
                stat=ChallengeStatus.WAITING_FOR_ACCEPTANCE;
                Assert.assertEquals(testHelper.resolveChallenge(arg1).getChallengeStatus(), stat);

        });

    }

}
