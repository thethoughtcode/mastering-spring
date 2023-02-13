package io.thoughtcode.springboot3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.thoughtcode.springboot3.entity.Privilege;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Optional<Privilege> findByName(String name);
}
