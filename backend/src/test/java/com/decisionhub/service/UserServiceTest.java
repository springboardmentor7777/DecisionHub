package com.decisionhub.service;

import com.decisionhub.dto.RegisterRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.exception.EmailAlreadyExistsException;
import com.decisionhub.repository.RoleRepository;
import com.decisionhub.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 *
 * NOTE: UserService.registerUser() is dead code — AuthService.register()
 * is the active registration path. These tests document the existing
 * UserService implementation for completeness and regression safety.
 *
 * Tests:
 *   registerUser() — 5 scenarios
 *     1. Success: new email, role found → UserResponse returned
 *     2. Email normalisation: trimmed and lowercased before lookup and storage
 *     3. Duplicate email → EmailAlreadyExistsException
 *     4. USER role absent in DB → IllegalStateException
 *     5. Returned UserResponse contains all five fields populated correctly
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService unit tests")
class UserServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private RoleRepository  roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private static final String EMAIL        = "alice@decisionhub.com";
    private static final String RAW_PASSWORD = "securePass1";
    private static final String ENCODED_HASH = "encodedHash_securePass1";
    private static final String FULL_NAME    = "Alice Smith";

    private Role    defaultRole;
    private User    savedUser;

    @BeforeEach
    void setUp() {
        defaultRole = new Role();
        defaultRole.setRoleId(1L);
        defaultRole.setRoleName("USER");

        savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setFullName(FULL_NAME);
        savedUser.setEmail(EMAIL);
        savedUser.setPasswordHash(ENCODED_HASH);
        savedUser.setRole(defaultRole);
        savedUser.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    private RegisterRequest request(String fullName, String email, String password) {
        RegisterRequest r = new RegisterRequest();
        r.setFullName(fullName);
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 1 — Happy path
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registerUser() returns UserResponse for a valid new registration")
    void registerUser_newEmail_returnsUserResponse() {

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_HASH);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.registerUser(request(FULL_NAME, EMAIL, RAW_PASSWORD));

        assertNotNull(response);
        assertEquals(1L,        response.getUserId());
        assertEquals(FULL_NAME, response.getFullName());
        assertEquals(EMAIL,     response.getEmail());
        assertEquals("USER",    response.getRole());
        assertNotNull(response.getCreatedAt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 2 — Email normalisation (trim + lowercase)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registerUser() trims and lowercases the email before saving")
    void registerUser_emailNormalised_trimmedAndLowercased() {

        String rawEmail = "  ALICE@DecisionHub.COM  ";
        String normalised = "alice@decisionhub.com";

        when(userRepository.existsByEmail(normalised)).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_HASH);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.registerUser(request(FULL_NAME, rawEmail, RAW_PASSWORD));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(normalised, captor.getValue().getEmail());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 3 — Duplicate email
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registerUser() throws EmailAlreadyExistsException for a duplicate email")
    void registerUser_duplicateEmail_throwsEmailAlreadyExistsException() {

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.registerUser(request(FULL_NAME, EMAIL, RAW_PASSWORD)));

        verify(userRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 4 — USER role missing
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registerUser() throws IllegalStateException when USER role is absent in DB")
    void registerUser_userRoleMissing_throwsIllegalStateException() {

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.registerUser(request(FULL_NAME, EMAIL, RAW_PASSWORD)));

        assertEquals("USER role not found in database", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 5 — Password is encoded, not stored in plain text
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registerUser() stores the encoded password hash, not the raw password")
    void registerUser_passwordIsEncoded() {

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_HASH);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.registerUser(request(FULL_NAME, EMAIL, RAW_PASSWORD));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        String storedHash = captor.getValue().getPasswordHash();
        assertEquals(ENCODED_HASH, storedHash);
        assertNotEquals(RAW_PASSWORD, storedHash);
        verify(passwordEncoder).encode(RAW_PASSWORD);
    }
}
