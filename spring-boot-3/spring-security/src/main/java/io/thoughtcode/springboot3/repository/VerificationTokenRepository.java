package io.thoughtcode.springboot3.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(final String token);

    Optional<VerificationToken> findByUser(User user);

    Collection<VerificationToken> findAllByExpiryDateLessThan(final OffsetDateTime now);

    void deleteByExpiryDateLessThan(final OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate <= ?1")
    void deleteAllExpiredSince(final OffsetDateTime now);
}
