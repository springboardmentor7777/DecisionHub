package com.decisionhub.service;

import com.decisionhub.dto.LoginRequest;
import com.decisionhub.dto.LoginResponse;
import com.decisionhub.dto.RegisterRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 *
 * All collaborators (AuthenticationManager, JwtService, UserRepository,
 * RoleRepository, PasswordEncoder) are mocked — no Spring context or
 * database connection is needed.
 *
 * Tests:
 *   register()        — 4 scenarios (success, email normalisation, duplicate, missing role)
 *   login()           — 2 scenarios (success, bad credentials)
 *   getCurrentUser()  — 2 scenarios (success, unknown email)
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService             jwtService;
    @Mock private UserRepository         userRepository;
    @Mock private RoleRepository         roleRepository;
    @Mock private PasswordEncoder        passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // ── Shared test fixtures ─────────────────────────────────────────────────

    private static final String EMAIL        = "alice@decisionhub.com";
    private static final String RAW_PASSWORD = "securePass1";
    private static final String ENCODED_HASH = "encodedHash_securePass1";
    private static final String FULL_NAME    = "Alice Smith";
    private static final String MOCK_TOKEN   = "mock.jwt.token";
    private static final Long   USER_ID      = 1L;

    private Role defaultRole;
    private User savedUser;

    @BeforeEach
    void setUp() {
        defaultRole = new Role();
        defaultRole.setRoleId(1L);
        defaultRole.setRoleName("USER");

        savedUser = new User();
        savedUser.setUserId(USER_ID);
        savedUser.setFullName(FULL_NAME);
        savedUser.setEmail(EMAIL);
        savedUser.setPasswordHash(ENCODED_HASH);
        savedUser.setRole(defaultRole);
        savedUser.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // register()
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Scenario 1 — Happy path: new email, USER role exists.
     *
     * register() must save the user and return a UserResponse populated
     * from the saved entity, including the role name from the roles table.
     */
    @Test
    @DisplayName("register() saves user and returns UserResponse for a new email")
    void register_newEmail_savesUserAndReturnsUserResponse() {

        RegisterRequest request = new RegisterRequest();
        request.setFullName(FULL_NAME);
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_HASH);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(USER_ID,   response.getUserId());
        assertEquals(FULL_NAME, response.getFullName());
        assertEquals(EMAIL,     response.getEmail());
        assertEquals("USER",    response.getRole());
        assertNotNull(response.getCreatedAt());

        verify(userRepository).save(any(User.class));
    }

    /**
     * Scenario 2 — Email normalisation: mixed case + surrounding spaces.
     *
     * register() calls email.trim().toLowerCase() before every repository
     * operation. This test confirms:
     *   - existsByEmail() receives the normalised email, not the raw input.
     *   - The User entity passed to save() carries the normalised email.
     *
     * Raw input:  "  ALICE@DECISIONHUB.COM  "
     * Stored as:  "alice@decisionhub.com"
     */
    @Test
    @DisplayName("register() trims and lowercases email before saving")
    void register_unnormalisedEmail_normalisesBeforeSave() {

        String rawEmail = "  ALICE@DECISIONHUB.COM  ";

        RegisterRequest request = new RegisterRequest();
        request.setFullName(FULL_NAME);
        request.setEmail(rawEmail);
        request.setPassword(RAW_PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_HASH);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(request);

        // The duplicate-check must use the normalised form
        verify(userRepository).existsByEmail(EMAIL);

        // Capture the User object actually passed to save() and inspect its email
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(EMAIL, captor.getValue().getEmail(),
                "User saved to the repository must have a normalised (trimmed + lowercased) email");
    }

    /**
     * Scenario 3 — Duplicate email → HTTP 409 CONFLICT.
     *
     * When existsByEmail() returns true, register() must throw
     * ResponseStatusException(CONFLICT, "Email is already registered")
     * immediately — without touching RoleRepository or saving anything.
     */
    @Test
    @DisplayName("register() throws ResponseStatusException(CONFLICT) for a duplicate email")
    void register_duplicateEmail_throwsConflict() {

        RegisterRequest request = new RegisterRequest();
        request.setFullName(FULL_NAME);
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Email is already registered"),
                "Exception reason must contain 'Email is already registered'");

        // No role lookup or save must happen after the early exit
        verify(roleRepository, never()).findByRoleName(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Scenario 4 — USER role missing from the database → IllegalStateException.
     *
     * If the roles table is empty or the seed data is absent, the orElseThrow
     * in register() must fire with the message "USER role not found in database".
     * No user must be saved.
     */
    @Test
    @DisplayName("register() throws IllegalStateException when the USER role is absent")
    void register_userRoleMissing_throwsIllegalStateException() {

        RegisterRequest request = new RegisterRequest();
        request.setFullName(FULL_NAME);
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> authService.register(request)
        );

        assertEquals("USER role not found in database", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // login()
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Scenario 5 — Successful login.
     *
     * login() must:
     *   1. Call authenticationManager.authenticate() — but NOT use its return value.
     *   2. Fetch the user from the repository.
     *   3. Call jwtService.generateToken(email).
     *   4. Return a LoginResponse containing the token and all user fields.
     *
     * NOTE: UsernamePasswordAuthenticationToken does not override equals(), so
     * we use argThat() to match on the actual principal and credentials fields
     * rather than comparing object references.
     */
    @Test
    @DisplayName("login() authenticates user and returns LoginResponse with a token")
    void login_validCredentials_returnsLoginResponse() {

        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);

        // authenticate() return value is discarded by login() — no stub needed.
        // Default Mockito behaviour (return null) is safe here.
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(EMAIL)).thenReturn(MOCK_TOKEN);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(MOCK_TOKEN, response.getAccessToken());
        assertEquals(USER_ID,    response.getUserId());
        assertEquals(FULL_NAME,  response.getFullName());
        assertEquals(EMAIL,      response.getEmail());
        assertEquals("USER",     response.getRole());

        // Verify authenticate() was called with the correct principal and credentials.
        // argThat() inspects the actual object fields because
        // UsernamePasswordAuthenticationToken does not implement equals().
        verify(authenticationManager).authenticate(argThat(token ->
                EMAIL.equals(token.getPrincipal()) &&
                RAW_PASSWORD.equals(token.getCredentials())));

        verify(jwtService).generateToken(EMAIL);
    }

    /**
     * Scenario 6 — Wrong password → BadCredentialsException propagated.
     *
     * When authenticationManager throws BadCredentialsException, login()
     * must let it propagate — the user lookup and token generation must
     * never happen.
     */
    @Test
    @DisplayName("login() propagates BadCredentialsException for wrong password")
    void login_wrongPassword_propagatesBadCredentialsException() {

        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword("wrongPassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request),
                "BadCredentialsException from authenticationManager must propagate"
        );

        // User lookup and token generation must NOT happen after auth failure
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService,     never()).generateToken(anyString());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getCurrentUser()
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Scenario 7 — Known email → UserResponse.
     *
     * getCurrentUser() must fetch the user and return a correctly populated
     * UserResponse using the same five-argument constructor used in register().
     */
    @Test
    @DisplayName("getCurrentUser() returns UserResponse for a known email")
    void getCurrentUser_knownEmail_returnsUserResponse() {

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(savedUser));

        UserResponse response = authService.getCurrentUser(EMAIL);

        assertNotNull(response);
        assertEquals(USER_ID,   response.getUserId());
        assertEquals(FULL_NAME, response.getFullName());
        assertEquals(EMAIL,     response.getEmail());
        assertEquals("USER",    response.getRole());
        assertNotNull(response.getCreatedAt());
    }

    /**
     * Scenario 8 — Unknown email → RuntimeException("User not found").
     *
     * getCurrentUser() uses orElseThrow(() -> new RuntimeException("User not found")).
     * This test confirms the exact exception type and message.
     */
    @Test
    @DisplayName("getCurrentUser() throws RuntimeException for an unknown email")
    void getCurrentUser_unknownEmail_throwsRuntimeException() {

        String unknownEmail = "ghost@decisionhub.com";
        when(userRepository.findByEmail(unknownEmail)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.getCurrentUser(unknownEmail)
        );

        assertEquals("User not found", ex.getMessage());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // login() — regression: user not found after successful authentication
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Scenario 9 — Successful authentication but user absent in DB → RuntimeException.
     *
     * This is a data-inconsistency / race-condition scenario (bug B1):
     *   1. authenticationManager.authenticate() SUCCEEDS — no exception thrown.
     *   2. userRepository.findByEmail() returns Optional.empty() — the user
     *      was deleted between authentication and the DB lookup.
     *
     * The current implementation throws RuntimeException("User not found"),
     * which propagates as HTTP 500. This test documents the existing behaviour
     * so that any future fix (e.g., changing to ResponseStatusException(NOT_FOUND))
     * is detected as a deliberate change rather than an accidental regression.
     *
     * The test also verifies that jwtService.generateToken() is NEVER called
     * when the user cannot be loaded from the repository.
     */
    @Test
    @DisplayName("login() throws RuntimeException when user is absent in DB after successful authentication (bug B1 regression)")
    void login_userNotFoundAfterSuccessfulAuthentication_throwsRuntimeException() {

        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);

        // authenticationManager.authenticate() does NOT throw —
        // Spring Security accepted the credentials.
        // Mockito's default behaviour (return null) is safe here.

        // DB lookup immediately after authentication returns nothing —
        // simulates deletion or inconsistency between the auth store and the DB.
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request),
                "login() must throw RuntimeException when the user cannot be found after authentication"
        );

        assertEquals("User not found", ex.getMessage());

        // JWT must never be generated if the user record is absent
        verify(jwtService, never()).generateToken(anyString());
    }
}
