package io.thoughtcode.springboot3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.entity.UserLocation;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    Optional<UserLocation> findByCountryAndUser(final String country, final User user);
}
