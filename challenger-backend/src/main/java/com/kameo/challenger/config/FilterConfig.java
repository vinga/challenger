package com.kameo.challenger.config;

import com.kameo.challenger.utils.auth.jwt.JWTService;
import com.kameo.challenger.utils.auth.jwt.JWTServiceConfig;
import com.kameo.challenger.web.rest.AuthFilter;
import com.kameo.challenger.web.rest.ChallengerSess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class FilterConfig {
    // althought AuthFilter is marked with component, we want it to be ingored, cause we manually add it in spring-security configuration at start
    @Bean
    public FilterRegistrationBean myAuthenticationFilterRegistration(final AuthFilter filter) {
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }

    @Value("${jwt.signingKey}")
    String signingKey;
    @Value("${jwt.issuer}")
    String issuer;
    @Value("${jwt.audience}")
    String audience;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public JWTService<ChallengerSess> jwtService() {
        JWTServiceConfig<ChallengerSess> conf = new JWTServiceConfig<>(signingKey
                .getBytes(), issuer, audience, ChallengerSess.class);
        return new JWTService<>(conf);
    }
}
