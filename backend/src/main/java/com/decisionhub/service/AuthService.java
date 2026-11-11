package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.AuthResponse;
import com.decisionhub.dto.ApiDtos.LoginRequest;
import com.decisionhub.dto.ApiDtos.RegisterRequest;
import com.decisionhub.entity.AppUser;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.security.JwtService;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        AppUser user = users.save(new AppUser(email, passwordEncoder.encode(request.password()), request.displayName().trim()));
        return response(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        AppUser user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return response(user);
    }

    @Transactional
    public AuthResponse oauthLogin(String email, String displayName) {
        String normalizedEmail = email.trim().toLowerCase();
        AppUser user = users.findByEmailIgnoreCase(normalizedEmail).orElseGet(() ->
                users.save(new AppUser(normalizedEmail,
                        passwordEncoder.encode(UUID.randomUUID().toString()),
                        displayName == null || displayName.isBlank() ? normalizedEmail : displayName)));
        return response(user);
    }

    private AuthResponse response(AppUser user) {
        return new AuthResponse(jwtService.generateToken(user), user.getId(), user.getEmail(),
                user.getDisplayName(), user.getRole());
    }
}
