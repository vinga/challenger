package com.kameo.challenger.config;

import com.kameo.challenger.web.rest.AuthFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

import static com.kameo.challenger.web.rest.AuthFilter.ROLE_USER;

@EnableWebSecurity
public class SecurityWebApplicationInitializer extends WebSecurityConfigurerAdapter {

    @Inject
    AuthFilter authFilter;

    public static class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {
        private static final long serialVersionUID = -8970718410437077606L;

        @Override
        public void commence(HttpServletRequest request,
                             HttpServletResponse response,
                             AuthenticationException authException) throws IOException {
            // This is invoked when user tries to access a secured REST resource without supplying any credentials
            // We should just send a 401 Unauthorized response because there is no 'login page' to redirect to
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // we don't need CSRF because our token is invulnerable
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint()).and()
                // don't create session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                //.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // allow anonymous resource requests


                .antMatchers(
                        HttpMethod.GET,
                        "/",
                        "/static/**.*",
                        "/favicon.ico"
                ).permitAll()

                .antMatchers(
                        "/api/accounts/newToken",
                        "/api/accounts/passwordReset",
                        "/api/accounts/register",
                        "/api/accounts/confirmationLinks/*",
                        "/oauth2/*"
                        //TODO swagger
                ).permitAll()

                .antMatchers("/api/**")
                .hasRole(ROLE_USER)


                .anyRequest().authenticated();
        // Custom JWT based security filter
        httpSecurity
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

    }



}