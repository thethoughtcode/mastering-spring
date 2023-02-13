package io.thoughtcode.springboot3.service;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import java.net.InetAddress;
import java.util.ArrayList;
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
import org.springframework.stereotype.Service;

import com.maxmind.geoip2.DatabaseReader;

import io.thoughtcode.springboot3.entity.NewLocationToken;
import io.thoughtcode.springboot3.entity.Privilege;
import io.thoughtcode.springboot3.entity.Role;
import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.entity.UserLocation;
import io.thoughtcode.springboot3.repository.NewLocationTokenRepository;
import io.thoughtcode.springboot3.repository.UserLocationRepository;
import io.thoughtcode.springboot3.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final Environment env;

    private final UserRepository repository;

    private final UserLocationRepository userLocationRepository;

    private final NewLocationTokenRepository newLocationTokenRepository;

    private final DatabaseReader geoipReader;

    // @formatter:off
    UserService(final Environment env,
                final UserRepository repository,
                final UserLocationRepository userLocationRepository,
                final NewLocationTokenRepository newLocationTokenRepository,
                final DatabaseReader geoipReader) {
        this.env = env;
        this.repository = repository;
        this.userLocationRepository = userLocationRepository;
        this.newLocationTokenRepository = newLocationTokenRepository;
        this.geoipReader = geoipReader;
    }
    // @formatter:on

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

    public User save(final User user) {
        return repository.save(user);
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

        try {
            final InetAddress ipAddress = InetAddress.getByName(ip);

            final String country = geoipReader.country(ipAddress).getCountry().getName();

            LOG.info("=======================**** Country - {}", country);

            final User user = repository.findByEmail(username).get();

            final Optional<UserLocation> location = userLocationRepository.findByCountryAndUser(country, user);

            if (location.isPresent()) {
                return createNewLocationToken(country, user);
            }

        } catch (final Exception e) {
            return null;
        }

        return null;
    }

    private NewLocationToken createNewLocationToken(final String country, User user) {
        final UserLocation userLocation = userLocationRepository.save(new UserLocation(country, user));
        return newLocationTokenRepository.save(new NewLocationToken(randomUUID().toString(), userLocation));
    }

    private boolean isGeoIpLibEnabled() {
        return Boolean.parseBoolean(env.getProperty("geo.ip.lib.enabled"));
    }
}
