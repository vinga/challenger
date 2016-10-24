package com.kameo.challenger.config;

import com.kameo.challenger.domain.accounts.AccountRestService;
import com.kameo.challenger.domain.challenges.ChallengeRestService;
import com.kameo.challenger.domain.events.EventGroupRestService;
import com.kameo.challenger.web.rest.impl.ChallengerRestService;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;


@Component
public class JerseyConfig extends ResourceConfig {

/*   public static  class MyContextResolver implements ContextResolver<ObjectMapper> {
       ObjectMapper om;
       {
           Jackson2ObjectMapperBuilder jb = new Jackson2ObjectMapperBuilder().modulesToInstall(new KotlinModule());
           om=(ObjectMapper)jb.build();
       }
       @Override
       public ObjectMapper getContext(Class<?> type) {
           return om;
       }
    }*/

    public JerseyConfig() {
        register(ChallengeRestService.class);
        register(ChallengerRestService.class);
        register(EventGroupRestService.class);
        register(AccountRestService.class);
//register(MyContextResolver.class);
        //    register(new MyContextResolver());
    }

}
