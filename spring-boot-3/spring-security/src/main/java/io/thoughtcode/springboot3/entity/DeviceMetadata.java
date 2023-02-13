package io.thoughtcode.springboot3.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class DeviceMetadata {

    @Id
    @Column(name = "dm_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "dm_user_id")
    private Long userId;

    @Column(name = "dm_device_details")
    private String deviceDetails;

    @Column(name = "dm_location")
    private String location;

    @Column(name = "dm_last_logged_in")
    private OffsetDateTime lastLoggedIn;
}
