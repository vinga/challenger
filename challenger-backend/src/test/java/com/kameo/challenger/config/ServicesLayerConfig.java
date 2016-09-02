package com.kameo.challenger.config;

import com.kameo.challenger.util.TestHelper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@TestConfiguration
@ComponentScan(basePackages = "com.kameo.challenger.services")
public class ServicesLayerConfig {

    @Bean
    TestHelper testHelper() {
        return new TestHelper();
    }
}
