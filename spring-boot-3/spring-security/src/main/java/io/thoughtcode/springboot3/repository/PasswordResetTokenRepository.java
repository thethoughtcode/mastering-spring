package io.thoughtcode.springboot3.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import io.thoughtcode.springboot3.entity.PasswordResetToken;
import io.thoughtcode.springboot3.entity.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(final String token);

    Optional<PasswordResetToken> findByUser(final User user);

    Collection<PasswordResetToken> findAllByExpiryDateLessThan(final OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate <= ?1")
    void deleteAllExpiredSince(final OffsetDateTime now);
}
