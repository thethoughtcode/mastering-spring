package io.thoughtcode.springboot3.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import io.thoughtcode.springboot3.entity.Privilege;
import io.thoughtcode.springboot3.entity.Role;
import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.repository.PrivilegeRepository;
import io.thoughtcode.springboot3.repository.RoleRepository;
import io.thoughtcode.springboot3.repository.UserRepository;
import jakarta.transaction.Transactional;

@Component
public class DatabaseInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PrivilegeRepository privilegeRepository;

    private final PasswordEncoder passwordEncoder;

    // @formatter:off
    public DatabaseInitializer(final UserRepository userRepository,
                               final RoleRepository roleRepository,
                               final PrivilegeRepository privilegeRepository,
                               final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.passwordEncoder = passwordEncoder;
    }
    // @formatter:on

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        if (alreadySetup) {
            return;
        }

        // == create initial privileges
        final Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        final Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
        final Privilege passwordPrivilege = createPrivilegeIfNotFound("CHANGE_PASSWORD_PRIVILEGE");

        // == create initial roles
        final List<Privilege> adminPrivileges = Arrays.asList(readPrivilege, writePrivilege, passwordPrivilege);
        final List<Privilege> userPrivileges = Arrays.asList(readPrivilege, passwordPrivilege);
        final Role adminRole = createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        createRoleIfNotFound("ROLE_USER", userPrivileges);

        // == create initial user
        createUserIfNotFound("test@test.com", "Test", "Test", "test", Arrays.asList(adminRole));

        alreadySetup = true;
    }

    @Transactional
    Privilege createPrivilegeIfNotFound(final String name) {

        final Privilege privilege;

        final Optional<Privilege> dbPrivilege = privilegeRepository.findByName(name);

        if (dbPrivilege.isEmpty()) {
            privilege = privilegeRepository.save(new Privilege(name));
        } else {
            privilege = dbPrivilege.get();
        }

        return privilege;
    }

    @Transactional
    Role createRoleIfNotFound(final String name, final Collection<Privilege> privileges) {

        final Role role;

        final Optional<Role> dbRole = roleRepository.findByName(name);

        if (dbRole.isEmpty()) {
            role = new Role(name);
        } else {
            role = dbRole.get();
        }

        role.setPrivileges(privileges);

        return roleRepository.save(role);
    }

    // @formatter:off
    @Transactional
    User createUserIfNotFound(final String email,
                              final String firstName,
                              final String lastName,
                              final String password,
                              final Collection<Role> roles) {

        final User user;

        final Optional<User> dbUser = userRepository.findByEmail(email);

        if (dbUser.isEmpty()) {
            user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setEnabled(true);
            user.setRoles(roles);
        } else {
            user = dbUser.get();
        }

        return userRepository.save(user);
    }
    // @formatter:on
}
