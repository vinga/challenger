package com.kameo.challenger;

import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.logic.FakeDataLogic;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.java8.En;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

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


    public Commons() {
        Given("^I am existing challenger user$", () -> {
            cmd.createUsers("myself");
        });
        Given("^my friend is existing challenger user$", () -> {
            cmd.createUsers("myFriend");
        });
    }

}
