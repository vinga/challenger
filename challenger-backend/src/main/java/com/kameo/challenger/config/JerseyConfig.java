package com.kameo.challenger.config;

import com.kameo.challenger.web.rest.impl.ChallengerRestService;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;


@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(ChallengerRestService.class);
    }

}
