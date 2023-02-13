package io.thoughtcode.springboot3.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ResourceUtils;

import com.maxmind.geoip2.DatabaseReader;

import io.thoughtcode.springboot3.config.auth.CustomAuthenticationProvider;
import io.thoughtcode.springboot3.config.auth.DifferentLocationChecker;
import io.thoughtcode.springboot3.config.dto.ActiveUserStore;
import io.thoughtcode.springboot3.repository.UserRepository;
import io.thoughtcode.springboot3.service.UserService;
import ua_parser.Parser;

@Configuration
public class AppConfig {

    @Bean
    ActiveUserStore activeUserStore() {
        return new ActiveUserStore();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    // @formatter:off
    @Bean
    DaoAuthenticationProvider authProvider(final UserRepository repository,
                                           final UserService userService,
                                           final PasswordEncoder encoder,
                                           final DifferentLocationChecker locationChecker) {

        final CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider(repository);

        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(encoder);
        authProvider.setPostAuthenticationChecks(locationChecker);

        return authProvider;
    }
    // @formatter:on

    @Bean
    DatabaseReader geoip2Reader() throws IOException {
        return new DatabaseReader.Builder(ResourceUtils.getFile("classpath:maxmind-geoip2/GeoLite2-City.mmdb")).build();
    }

    @Bean
    Parser uaParser() throws IOException {
        return new Parser();
    }
}
