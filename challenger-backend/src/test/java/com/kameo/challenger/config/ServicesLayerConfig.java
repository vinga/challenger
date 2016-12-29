package com.kameo.challenger.config;

import com.kameo.challenger.domain.challenges.IChallengeRestService;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.util.TestHelper.TransactionProxy;
import com.kameo.challenger.utils.mail.MailService;
import com.kameo.challenger.web.rest.ChallengerSess;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


@TestConfiguration
@ComponentScan(basePackages = {"com.kameo.challenger.logic","com.kameo.challenger.domain"}, excludeFilters={
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=IChallengeRestService.class)})
public class ServicesLayerConfig {

    @Bean
    ServerConfig serverConfig() {
        return new ServerConfig();
    }
    @Bean
    TestHelper testHelper() {
        return new TestHelper();
    }
    @Bean
    TransactionProxy transactionProxy() { return new TransactionProxy(); }

    @Bean
    ChallengerSess sess() {
        return new ChallengerSess();
    }
    @Bean
    MailService mailService() {
      return new MailService() {
          @Override
          public void send(Message m) {
              testHelper().getSentMessagesList().add(m);
          }
      };
    }
}
