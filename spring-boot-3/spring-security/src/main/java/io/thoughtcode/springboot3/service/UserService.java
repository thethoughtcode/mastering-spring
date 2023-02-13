package io.thoughtcode.springboot3.service;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.maxmind.geoip2.DatabaseReader;

import io.thoughtcode.springboot3.entity.NewLocationToken;
import io.thoughtcode.springboot3.entity.Privilege;
import io.thoughtcode.springboot3.entity.Role;
import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.entity.UserLocation;
import io.thoughtcode.springboot3.entity.VerificationToken;
import io.thoughtcode.springboot3.repository.NewLocationTokenRepository;
import io.thoughtcode.springboot3.repository.RoleRepository;
import io.thoughtcode.springboot3.repository.UserLocationRepository;
import io.thoughtcode.springboot3.repository.UserRepository;
import io.thoughtcode.springboot3.repository.VerificationTokenRepository;
import io.thoughtcode.springboot3.web.dto.UserDto;
import io.thoughtcode.springboot3.web.error.UserAlreadyExistException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final Environment env;

    private final UserRepository repository;

    private final RoleRepository roleRepository;

    private final UserLocationRepository userLocationRepository;

    private final NewLocationTokenRepository newLocationTokenRepository;

    private final VerificationTokenRepository verificationTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final DatabaseReader geoipReader;

    // @formatter:off
    UserService(final Environment env,
                final UserRepository repository,
                final RoleRepository roleRepository,
                final UserLocationRepository userLocationRepository,
                final NewLocationTokenRepository newLocationTokenRepository,
                final VerificationTokenRepository verificationTokenRepository,
                final PasswordEncoder passwordEncoder,
                final DatabaseReader geoipReader) {
        this.env = env;
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.userLocationRepository = userLocationRepository;
        this.newLocationTokenRepository = newLocationTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.geoipReader = geoipReader;
    }
    // @formatter:on

    public User registerNewUserAccount(final UserDto dto) {

        if (emailExists(dto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: " + dto.getEmail());
        }
        final User user = new User();

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setUsing2FA(dto.isUsing2FA());
        user.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER").get()));

        return repository.save(user);
    }

    private boolean emailExists(final String email) {
        return repository.findByEmail(email).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {

        final Optional<User> record = repository.findByEmail(email);

        if (record.isEmpty()) {
            throw new UsernameNotFoundException("User not found - " + email);
        }

        final User user = record.get();

        // @formatter:off
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                getAuthorities(user.getRoles())
        );
        // @formatter:on
    }

    private Collection<? extends GrantedAuthority> getAuthorities(final Collection<Role> roles) {
        return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(final Collection<Role> roles) {

        // TODO: This can be adjusted again
        final List<String> privileges = new ArrayList<>();
        final List<Privilege> collection = new ArrayList<>();

        for (final Role role : roles) {
            privileges.add(role.getName());
            collection.addAll(role.getPrivileges());
        }

        for (final Privilege item : collection) {
            privileges.add(item.getName());
        }

        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(final List<String> privileges) {
        return privileges.stream().map(SimpleGrantedAuthority::new).collect(toList());
    }

    public NewLocationToken isNewLoginLocation(final String username, final String ip) {

        if (!isGeoIpLibEnabled()) {
            return null;
        }

        final String country = getIpLocationCountry(ip);

        LOG.info("=======================**** Country - {}", country);

        final User user = repository.findByEmail(username).get();

        final Optional<UserLocation> location = userLocationRepository.findByCountryAndUser(country, user);

        return location.isPresent() ? createNewLocationToken(country, user) : null;
    }

    public void addUserLocation(final User user, String ip) {

        if (!isGeoIpLibEnabled()) {
            return;
        }

        final String country = getIpLocationCountry(ip);
        userLocationRepository.save(new UserLocation(country, user, true));
    }

    public void createVerificationTokenForUser(final User user, final String token) {
        verificationTokenRepository.save(new VerificationToken(token, user));
    }

    private String getIpLocationCountry(final String ip) {
        try {
            return geoipReader.country(InetAddress.getByName(ip)).getCountry().getName();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private NewLocationToken createNewLocationToken(final String country, User user) {
        final UserLocation userLocation = userLocationRepository.save(new UserLocation(country, user));
        return newLocationTokenRepository.save(new NewLocationToken(randomUUID().toString(), userLocation));
    }

    private boolean isGeoIpLibEnabled() {
        return Boolean.parseBoolean(env.getProperty("geo.ip.lib.enabled"));
    }
}
