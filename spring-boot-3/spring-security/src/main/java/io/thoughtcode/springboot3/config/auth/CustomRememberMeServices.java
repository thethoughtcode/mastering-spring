package io.thoughtcode.springboot3.config.auth;

import java.util.Date;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomRememberMeServices extends PersistentTokenBasedRememberMeServices {

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    private final UserRepository userRepository;

    private PersistentTokenRepository tokenRepository;

    // @formatter:off
    public CustomRememberMeServices(final String key,
                                    final UserDetailsService userDetailsService,
                                    final UserRepository userRepository,
                                    final PersistentTokenRepository tokenRepository) {
        super(key, userDetailsService, tokenRepository);
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }
    // @formatter:on

    @Override
    protected void onLoginSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) {

        final String username = ((User) authentication.getPrincipal()).getEmail();

        logger.debug("Creating new persistent login for user " + username);

        final PersistentRememberMeToken token = new PersistentRememberMeToken(username, generateSeriesData(), generateTokenData(), new Date());

        try {
            tokenRepository.createNewToken(token);
            addCookie(token, request, response);
        } catch (final Exception e) {
            logger.error("Failed to save persistent token ", e);
        }
    }

    @Override
    protected Authentication createSuccessfulAuthentication(final HttpServletRequest request, final UserDetails user) {

        final User auser = userRepository.findByEmail(user.getUsername()).get();

        final RememberMeAuthenticationToken auth = new RememberMeAuthenticationToken(getKey(), auser, authoritiesMapper.mapAuthorities(user.getAuthorities()));

        auth.setDetails(authenticationDetailsSource.buildDetails(request));

        return auth;
    }

    private void addCookie(final PersistentRememberMeToken token, final HttpServletRequest request, final HttpServletResponse response) {
        setCookie(new String[] { token.getSeries(), token.getTokenValue() }, getTokenValiditySeconds(), request, response);
    }
}
