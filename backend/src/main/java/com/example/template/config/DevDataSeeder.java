package com.example.template.config;

import com.example.template.user.entity.User;
import com.example.template.user.repository.RoleRepository;
import com.example.template.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a dev admin account on startup — only active when spring.profiles.active=dev.
 * Credentials: admin@example.com / Admin@123456
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@example.com")) {
            return;
        }

        User admin = User.builder()
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("Admin@123456"))
                .enabled(true)
                .build();

        roleRepository.findByName("ROLE_ADMIN").ifPresent(r -> admin.getRoles().add(r));
        roleRepository.findByName("ROLE_USER").ifPresent(r -> admin.getRoles().add(r));

        userRepository.save(admin);
        log.info("Dev admin seeded — email: admin@example.com  password: Admin@123456");
    }
}
