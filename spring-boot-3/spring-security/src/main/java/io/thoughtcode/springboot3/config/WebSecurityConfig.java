package io.thoughtcode.springboot3.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
            "/resources/**",
            "/login*",
            "/logout*",
            "/registration*",
            "/success_register*",
            "/api/user/registration*"
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
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/h2/**");
    }

    @Bean
    SecurityFilterChain configure(final HttpSecurity http) throws Exception {
        // @formatter:off
        return http
                .csrf(request -> request.disable())
                .authorizeHttpRequests(request ->
                    request
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers("/invalid_session*").anonymous()
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
                        .and()
                        .sessionFixation().none()
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
