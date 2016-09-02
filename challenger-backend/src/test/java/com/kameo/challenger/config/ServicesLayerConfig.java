package com.kameo.challenger.config;

import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.MailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@TestConfiguration
@ComponentScan(basePackages = "com.kameo.challenger.logic")
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
