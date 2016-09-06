package com.kameo.challenger;


import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@AutoConfigureDataJpa
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class ChallengesTest implements En {
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


    public ChallengesTest() {

        Given("^I have (\\d+) accepted challenge? with my friend$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            for (int i=0; i<arg1; i++)
                testHelper.createAcceptedChallenge(users.iterator());

        });

        Given("^I have (\\d+) challenge? sent by me to my friend waiting for acceptance$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            for (int i=0; i<arg1; i++)
                testHelper.createPendingChallenge(users.iterator());

        });


        Given("^I have (\\d+) challenge waiting for my acceptance$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            Collections.reverse(users);
            for (int i=0; i<arg1; i++)
                testHelper.createPendingChallenge(users.iterator());

        });

        Given("^I have (\\d+) challenge rejected by me$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            Collections.reverse(users);
            for (int i=0; i<arg1; i++)
                testHelper.createRejectedChallenge(users.iterator());
        });

        Given("^I have (\\d+) challenge sent by me and rejected by my friend$", (Integer arg1) -> {
            List<UserODB> users = testHelper.createUsers("myself", "myFriend");
            for (int i=0; i<arg1; i++)
                testHelper.createRejectedChallenge(users.iterator());
        });



        Then("^I should see (\\d+) challenges on my list$", (Integer arg1) -> {
            UserODB myself = testHelper.myself();
            ChallengerLogic.ChallengeContractInfoDTO res = challengerService
                    .getVisibleChallengeContracts(myself.getId());
            Assert.assertEquals(arg1.longValue(),res.getVisibleChallenges().size());

        });

        Then("^see which challenge was recently chosen from the list$", () -> {
            UserODB myself = testHelper.myself();
            ChallengerLogic.ChallengeContractInfoDTO res = challengerService
                    .getVisibleChallengeContracts(myself.getId());
            Assert.assertNotNull(res.getDefaultChallengeId());
        });

    }


}
