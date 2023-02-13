package io.thoughtcode.springboot3.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = "user")
public class VerificationToken {

    private static final int EXPIRATION_MINUTES = 60 * 24;

    @Id
    @Column(name = "vt_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "vt_token")
    private String token;

    @OneToOne(targetEntity = User.class)
    @JoinColumn(nullable = false, name = "vt_user_id", foreignKey = @ForeignKey(name = "FK_VERIFY_USER"))
    private User user;

    @Column(name = "vt_expiry_date")
    private OffsetDateTime expiryDate;

    public VerificationToken() {
        super();
    }

    public VerificationToken(final String token) {
        super();
        this.token = token;
        this.expiryDate = OffsetDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }

    public VerificationToken(final String token, final User user) {
        super();
        this.token = token;
        this.user = user;
        this.expiryDate = OffsetDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }
}
