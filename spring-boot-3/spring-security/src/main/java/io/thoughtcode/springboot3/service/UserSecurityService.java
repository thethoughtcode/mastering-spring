package io.thoughtcode.springboot3.service;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.thoughtcode.springboot3.entity.PasswordResetToken;
import io.thoughtcode.springboot3.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserSecurityService {

    private final PasswordResetTokenRepository repository;

    static final String INVALID_TOKEN = "invalidToken";

    static final String EXPIRED_TOKEN = "expired";

    UserSecurityService(final PasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    public String validatePasswordResetToken(final String token) {

        final Optional<PasswordResetToken> dbToken = repository.findByToken(token);

        final String result;

        if (isTokenEmpty(dbToken)) {
            result = INVALID_TOKEN;
        } else if (isTokenExpired(dbToken)) {
            result = EXPIRED_TOKEN;
        } else {
            result = null;
        }

        return result;
    }

    private boolean isTokenEmpty(final Optional<PasswordResetToken> token) {
        return token.isEmpty();
    }

    private boolean isTokenExpired(final Optional<PasswordResetToken> token) {
        return token.get().getExpiryDate().isBefore(OffsetDateTime.now());
    }
}
