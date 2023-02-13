package io.thoughtcode.springboot3.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.RememberMeServices;

import io.thoughtcode.springboot3.config.auth.UserLoginSuccessHandler;
import io.thoughtcode.springboot3.config.auth.UserLogoutSuccessHandler;

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
    private UserLoginSuccessHandler loginSuccessHandler;

    @Autowired
    private UserLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler loginFailureHandler;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private RememberMeServices rememberMeServices;

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
                        .failureHandler(loginFailureHandler)
                )
                .sessionManagement(request ->
                    request
                        .invalidSessionUrl("/invalid_session.html")
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry)
                        // sessionFixation.none
                )
                .logout(request ->
                    request
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl("/logout.html?logSucc=true")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .rememberMe(request -> request.rememberMeServices(rememberMeServices))
                .build();
        // @formatter:on
    }
}
