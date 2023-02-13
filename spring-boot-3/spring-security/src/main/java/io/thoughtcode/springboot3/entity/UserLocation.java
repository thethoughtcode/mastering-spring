package io.thoughtcode.springboot3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = "user")
public class UserLocation {

    @Id
    @Column(name = "ul_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "ul_country")
    private String country;

    @Column(name = "ul_enabled")
    private boolean enabled;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(nullable = false, name = "ul_user_id")
    private User user;

    public UserLocation() {
        super();
        enabled = false;
    }

    public UserLocation(final String country, final User user) {
        super();
        this.country = country;
        this.user = user;
        this.enabled = false;
    }
}
