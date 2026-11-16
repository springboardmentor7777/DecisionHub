package com.decisionhub.service;

import com.decisionhub.dto.AuthDtos.AuthResponse;
import com.decisionhub.dto.AuthDtos.LoginRequest;
import com.decisionhub.dto.AuthDtos.RegisterRequest;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.exception.DuplicateResourceException;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        String email = normalize(req.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }
        User user = userRepository.save(User.builder()
                .name(req.name().trim())
                .email(email)
                .password(passwordEncoder.encode(req.password()))
                .role(Role.USER)   // self-registration is ALWAYS USER; admins promote others
                .build());
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        String email = normalize(req.email());
        // throws BadCredentialsException (401) / DisabledException (403)
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        User user = userRepository.findByEmail(email).orElseThrow();
        return toResponse(user);
    }

    private AuthResponse toResponse(User u) {
        return new AuthResponse(jwtService.generateToken(u.getEmail(), u.getRole().name()),
                u.getName(), u.getEmail(), u.getRole().name());
    }

    private static String normalize(String email) { return email.trim().toLowerCase(); }
}