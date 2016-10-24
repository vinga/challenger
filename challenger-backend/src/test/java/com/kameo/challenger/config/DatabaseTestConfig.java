package com.kameo.challenger.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfigureDataJpa
@EntityScan(basePackages ={"com.kameo.challenger.domain"})
@TestConfiguration
@Import(DatabaseConfig.class)
public class DatabaseTestConfig {




}
