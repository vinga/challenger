package com.kameo.challenger.config;

import com.challenger.eviauth.web.rest.impl.TestService;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

/**
 * Created by kmyczkowska on 2016-08-30.
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(TestService.class);
    }

}
