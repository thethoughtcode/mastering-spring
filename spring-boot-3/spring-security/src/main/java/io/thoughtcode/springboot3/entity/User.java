package io.thoughtcode.springboot3.entity;

import java.util.Collection;

import org.jboss.aerogear.security.otp.api.Base32;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "user_account")
@Data
@ToString(exclude = "roles")
public class User {

    @Id
    @Column(name = "u_id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "u_first_name")
    private String firstName;

    @Column(name = "u_last_name")
    private String lastName;

    @Column(name = "u_email")
    private String email;

    @Column(name = "u_password", length = 60)
    private String password;

    @Column(name = "u_enabled")
    private boolean enabled;

    @Column(name = "u_using2FA")
    private boolean isUsing2FA;

    @Column(name = "u_secret")
    private String secret;

    @ManyToMany
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "u_id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "r_id"))
    private Collection<Role> roles;

    public User() {
        super();
        this.secret = Base32.random();
        this.enabled = false;
    }
}
