package io.thoughtcode.springboot3.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import io.thoughtcode.springboot3.entity.DeviceMetadata;

public interface DeviceMetadataRepository extends JpaRepository<DeviceMetadata, Long> {

    Collection<DeviceMetadata> findByUserId(final Long userId);
}
