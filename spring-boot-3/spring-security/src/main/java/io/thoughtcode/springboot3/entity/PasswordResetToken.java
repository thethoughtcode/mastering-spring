package io.thoughtcode.springboot3.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class PasswordResetToken {

    private static final int EXPIRATION_MINUTES = 60 * 24;

    @Id
    @Column(name = "t_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "t_token")
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(name = "t_expiry_date")
    private OffsetDateTime expiryDate;

    public PasswordResetToken(final String token) {
        this.token = token;
        this.expiryDate = OffsetDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }
}
