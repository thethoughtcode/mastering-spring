package io.thoughtcode.springboot3.config.auth;

import java.util.Optional;

import org.jboss.aerogear.security.otp.Totp;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import io.thoughtcode.springboot3.config.g2fa.GoogleTotpAuthenticationDetails;
import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.repository.UserRepository;

public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    private final UserRepository repository;

    public CustomAuthenticationProvider(final UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Authentication authenticate(final Authentication auth) throws AuthenticationException {

        final Optional<User> dbUser = repository.findByEmail(auth.getName());

        if (dbUser.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        final User user = dbUser.get();

        if (user.isUsing2FA()) {

            final String verificationCode = ((GoogleTotpAuthenticationDetails) auth.getDetails()).getVerificationCode();

            final Totp totp = new Totp(user.getSecret());

            if (!isValidLong(verificationCode) || !totp.verify(verificationCode)) {
                throw new BadCredentialsException("Invalid verification code");
            }
        }
        final Authentication result = super.authenticate(auth);
        return new UsernamePasswordAuthenticationToken(user, result.getCredentials(), result.getAuthorities());
    }

    private boolean isValidLong(final String code) {
        try {
            Long.parseLong(code);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
