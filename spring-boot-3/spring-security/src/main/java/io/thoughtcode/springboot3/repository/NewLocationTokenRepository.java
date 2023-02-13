package io.thoughtcode.springboot3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.thoughtcode.springboot3.entity.NewLocationToken;
import io.thoughtcode.springboot3.entity.UserLocation;

public interface NewLocationTokenRepository extends JpaRepository<NewLocationToken, Long> {

    Optional<NewLocationToken> findByToken(final String token);

    Optional<NewLocationToken> findByUserLocation(final UserLocation userLocation);
}
