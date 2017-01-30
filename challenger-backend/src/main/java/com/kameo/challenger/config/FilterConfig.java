package com.kameo.challenger.config;

import com.kameo.challenger.web.rest.AuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
