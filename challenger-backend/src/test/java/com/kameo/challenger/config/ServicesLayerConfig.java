package com.kameo.challenger.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;


@TestConfiguration
@ComponentScan(basePackages = "com.kameo.challenger.services")
public class ServicesLayerConfig {
}
