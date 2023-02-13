package io.thoughtcode.springboot3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = "userLocation")
public class NewLocationToken {

    @Id
    @Column(name = "nlt_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "nlt_token")
    private String token;

    @OneToOne(targetEntity = UserLocation.class)
    @JoinColumn(nullable = false, name = "nlt_user_location_id")
    private UserLocation userLocation;

    public NewLocationToken() {
        super();
    }

    public NewLocationToken(final String token) {
        super();
        this.token = token;
    }

    public NewLocationToken(final String token, final UserLocation userLocation) {
        super();
        this.token = token;
        this.userLocation = userLocation;
    }
}
