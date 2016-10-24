package com.kameo.challenger.config

//import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
open class RestServiceConfig {

  /*  @Bean
    open fun jacksonNoArgsKotlinSupport(): Jackson2ObjectMapperBuilder {
        return Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())
    }*/
}