package io.thoughtcode.springboot3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.thoughtcode.springboot3.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
