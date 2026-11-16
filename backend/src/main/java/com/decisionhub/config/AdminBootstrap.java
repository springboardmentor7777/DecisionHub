package com.decisionhub.config;

import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.email:}") private String email;
    @Value("${app.bootstrap-admin.password:}") private String password;

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) return;
        String normalized = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalized)) return;
        userRepository.save(User.builder()
                .name("Administrator")
                .email(normalized)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .build());
        log.info("Bootstrap admin created: {}", normalized);
    }
}