package io.thoughtcode.springboot3.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import io.thoughtcode.springboot3.config.auth.LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    // @formatter:off
    private static final String[] PUBLIC_PATHS = {
            "/login*",
            "/logout*"
    };
    // @formatter:on

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Bean
    SecurityFilterChain configure(final HttpSecurity http) throws Exception {
        // @formatter:off
        return http
                .csrf(request -> request.disable())
                .authorizeHttpRequests(requests ->
                    requests
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(requests ->
                    requests
                        .loginPage("/login")
                        .defaultSuccessUrl("/homepage.html")
                        .failureUrl("/login?error=true")
                        .successHandler(loginSuccessHandler)
                )
                .build();
        // @formatter:on
    }
}
