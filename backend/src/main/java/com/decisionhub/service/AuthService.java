
package com.decisionhub.service;

import com.decisionhub.dto.UserResponse;
import com.decisionhub.dto.LoginRequest;
import com.decisionhub.dto.LoginResponse;
import com.decisionhub.dto.RegisterRequest;
import com.decisionhub.entity.User;
import com.decisionhub.entity.Role;
import com.decisionhub.repository.UserRepository;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public AuthService(
                        AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {

                this.authenticationManager = authenticationManager;
                this.jwtService = jwtService;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
        }

        // REGISTER
        public UserResponse register(RegisterRequest request) {

                String email = request.getEmail().trim().toLowerCase();

                if (userRepository.existsByEmail(email)) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Email is already registered");
                }

                User user = new User();

                user.setFullName(request.getFullName().trim());
                user.setEmail(email);
                user.setPasswordHash(
                                passwordEncoder.encode(request.getPassword()));
                user.setRole(Role.USER);
                user.setCreatedAt(LocalDateTime.now());

                User savedUser = userRepository.save(user);

                return new UserResponse(
                                savedUser.getUserId(),
                                savedUser.getFullName(),
                                savedUser.getEmail(),
                                savedUser.getRole(),
                                savedUser.getCreatedAt());
        }

        // LOGIN
        public LoginResponse login(LoginRequest request) {

                String email = request.getEmail().trim().toLowerCase();

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                email,
                                                request.getPassword()));

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String token = jwtService.generateToken(user.getEmail());

                return new LoginResponse(
                                token,
                                user.getUserId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getRole());
        }

        // GET CURRENT USER
        public UserResponse getCurrentUser(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                return new UserResponse(
                                user.getUserId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getRole(),
                                user.getCreatedAt());
        }
}
