package io.thoughtcode.springboot3.service;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.thoughtcode.springboot3.entity.DeviceMetadata;
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

    public static final String TOKEN_INVALID = "invalidToken";

    public static final String TOKEN_EXPIRED = "expired";

    public static final String TOKEN_VALID = "valid";

    private final UserRepository repository;

    private final RoleRepository roleRepository;

    private final UserLocationRepository userLocationRepository;

    private final NewLocationTokenRepository newLocationTokenRepository;

    private final VerificationTokenRepository verificationTokenRepository;

    private final DeviceMetadataService deviceMetadataService;

    private final PasswordEncoder passwordEncoder;

    // @formatter:off
    UserService(final UserRepository repository,
                final RoleRepository roleRepository,
                final UserLocationRepository userLocationRepository,
                final NewLocationTokenRepository newLocationTokenRepository,
                final VerificationTokenRepository verificationTokenRepository,
                final DeviceMetadataService deviceMetadataService,
                final PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.userLocationRepository = userLocationRepository;
        this.newLocationTokenRepository = newLocationTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.deviceMetadataService = deviceMetadataService;
        this.passwordEncoder = passwordEncoder;
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

    public User getUser(final String verificationToken) {

        final Optional<VerificationToken> token = verificationTokenRepository.findByToken(verificationToken);

        return token.isPresent() ? token.get().getUser() : null;
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

        if (!deviceMetadataService.isGeoIpLibEnabled()) {
            return null;
        }

        final String country = deviceMetadataService.getIpLocationCountry(ip);

        LOG.info("=======================**** Country - {}", country);

        final User user = repository.findByEmail(username).get();

        final Optional<UserLocation> location = userLocationRepository.findByCountryAndUser(country, user);

        return location.isEmpty() || !location.get().isEnabled() ? createNewLocationToken(country, user) : null;
    }

    public void addUserLocation(final User user, String ip) {

        if (!deviceMetadataService.isGeoIpLibEnabled()) {
            return;
        }

        final String country = deviceMetadataService.getIpLocationCountry(ip);
        userLocationRepository.save(new UserLocation(country, user, true));
    }

    public void createVerificationTokenForUser(final User user, final String token) {
        verificationTokenRepository.save(new VerificationToken(token, user));
    }

    public String validateVerificationToken(final String token, final String ip, final String deviceDetails) {

        final Optional<VerificationToken> verificationTokenEntry = verificationTokenRepository.findByToken(token);

        if (verificationTokenEntry.isEmpty()) {
            return TOKEN_INVALID;
        }

        final VerificationToken verificationToken = verificationTokenEntry.get();

        if (verificationToken.getExpiryDate().isBefore(OffsetDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            return TOKEN_EXPIRED;
        }

        final User user = verificationToken.getUser();
        user.setEnabled(true);

        repository.save(user);

        final DeviceMetadata device = deviceMetadataService.addDevice(user, ip, deviceDetails);

        LOG.info("User - {}", user);
        LOG.info("Device - {}", device);

        return TOKEN_VALID;
    }

    private NewLocationToken createNewLocationToken(final String country, User user) {
        final UserLocation userLocation = userLocationRepository.save(new UserLocation(country, user));
        return newLocationTokenRepository.save(new NewLocationToken(randomUUID().toString(), userLocation));
    }

    public Object generateQRUrl(final User user) {
        // TODO Auto-generated method stub
        return null;
    }
}
