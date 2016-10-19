package com.kameo.challenger.config;

import com.kameo.challenger.domain.challenges.IChallengeRestService;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.MailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


@TestConfiguration
@ComponentScan(basePackages = {"com.kameo.challenger.logic","com.kameo.challenger.domain"}, excludeFilters={
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=IChallengeRestService.class)})
public class ServicesLayerConfig {

    @Bean
    TestHelper testHelper() {
        return new TestHelper();
    }

    @Bean
    MailService mailService() {
      return new MailService() {
          @Override
          public void sendHtml(Message m) {
              testHelper().getSentMessagesList().add(m);
          }
      };
    };
}
