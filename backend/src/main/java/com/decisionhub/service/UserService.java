package com.decisionhub.service;

import com.decisionhub.dto.UserResponse;
import com.decisionhub.dto.RegisterRequest;
import com.decisionhub.entity.User;
import com.decisionhub.entity.Role;
import com.decisionhub.exception.EmailAlreadyExistsException;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.repository.RoleRepository;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @deprecated B8 — Dead code. Registration is handled exclusively by
     * {@link AuthService#register(RegisterRequest)}. This method duplicates that
     * logic and is not called from any controller or test. Retained to avoid
     * unintended breakage of any future callers; remove in a dedicated cleanup PR.
     */
    @Deprecated(since = "2026-11-16", forRemoval = true)
    public UserResponse registerUser(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email already registered");
        }

        Role defaultRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException(
                        "USER role not found in database"));

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));
        user.setRole(defaultRole);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getUserId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().getRoleName(),
                savedUser.getCreatedAt());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getRoleName(),
                user.getCreatedAt());
    }

    public UserResponse updateUser(Long id, String fullName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName.trim());
        }
        User saved = userRepository.save(user);
        return new UserResponse(
                saved.getUserId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getRole().getRoleName(),
                saved.getCreatedAt());
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getRoleName(),
                user.getCreatedAt());
    }
}
