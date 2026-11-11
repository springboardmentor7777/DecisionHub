
package com.decisionhub.service;

import com.decisionhub.dto.RegisterRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.entity.User;
import com.decisionhub.entity.Role;
import com.decisionhub.exception.EmailAlreadyExistsException;
import com.decisionhub.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse registerUser(RegisterRequest request) {

        // Normalize email
        String email = request.getEmail().trim().toLowerCase();

        // Check for duplicate email
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                "Email already registered"
            );
        }

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);

        // Encode password before storing
        user.setPasswordHash(
            passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return new UserResponse(
            savedUser.getUserId(),
            savedUser.getFullName(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getCreatedAt()
        );
    }
}
