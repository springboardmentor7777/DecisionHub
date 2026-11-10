package com.decisionhub.service;

import com.decisionhub.dto.AuthDtos.*;
import com.decisionhub.entity.*;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User user = userRepository.save(User.builder()
                .name(req.name())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .role(Role.USER)            // everyone starts as USER
                .build());
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepository.findByEmail(req.email()).orElseThrow();
        return toResponse(user);
    }

    private AuthResponse toResponse(User u) {
        String token = jwtService.generateToken(u.getEmail(), u.getRole().name());
        return new AuthResponse(token, u.getName(), u.getEmail(), u.getRole().name());
    }
}