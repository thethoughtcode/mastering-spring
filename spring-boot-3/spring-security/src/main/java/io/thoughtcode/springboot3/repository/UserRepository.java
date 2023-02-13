package io.thoughtcode.springboot3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.thoughtcode.springboot3.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(final String email);
}
