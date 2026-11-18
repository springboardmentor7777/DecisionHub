package com.decisionhub.controller;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.LoginRequest;
import com.decisionhub.dto.LoginResponse;
import com.decisionhub.dto.RegisterRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.service.AuthService;
import com.decisionhub.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.JwtException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for AuthController using @WebMvcTest.
 *
 * Only the web layer is loaded — no database, no full application context.
 * Spring Security IS active (SecurityConfig is applied) so auth rules are real.
 *
 * Four @MockitoBean declarations supply every bean the Security context needs:
 *   - AuthService        : direct collaborator of AuthController
 *   - JwtService         : injected into JwtAuthenticationFilter (@Component)
 *   - UserDetailsService : injected into JwtAuthenticationFilter + SecurityConfig
 *   - PasswordEncoder    : injected into SecurityConfig.authenticationProvider()
 *
 * SecurityConfig.authenticationEntryPoint = HttpStatusEntryPoint(UNAUTHORIZED),
 * so unauthenticated requests to protected routes return HTTP 401 (not 403).
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    // ── Shared fixtures ──────────────────────────────────────────────────────

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL    = "/api/auth/login";

    private UserResponse stubUserResponse() {
        return new UserResponse(
                1L, "Alice Smith", "alice@decisionhub.com",
                "USER", LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    private LoginResponse stubLoginResponse() {
        return new LoginResponse(
                "mock.jwt.token", 1L,
                "Alice Smith", "alice@decisionhub.com", "USER", 3600L);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POST /api/auth/register
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Scenario 1 — Successful registration → HTTP 201 CREATED.
     *
     * A fully valid RegisterRequest is posted. AuthService.register() returns
     * a UserResponse. AuthController wraps it in ResponseEntity.status(201).body(...).
     */
    @Test
    @DisplayName("POST /api/auth/register → 201 for a valid request")
    void register_validRequest_returns201() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Alice Smith");
        request.setEmail("alice@decisionhub.com");
        request.setPassword("securePass1");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(stubUserResponse());

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@decisionhub.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    /**
     * Scenario 2a — Blank fullName → HTTP 400 BAD REQUEST.
     *
     * @NotBlank on RegisterRequest.fullName triggers MethodArgumentNotValidException.
     * GlobalExceptionHandler catches it and returns HTTP 400 with field → message map.
     * We assert that the "fullName" key is present in the response body.
     */
    @Test
    @DisplayName("POST /api/auth/register → 400 when fullName is blank")
    void register_blankFullName_returns400() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("");                       // violates @NotBlank
        request.setEmail("alice@decisionhub.com");
        request.setPassword("securePass1");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fullName").exists());
    }

    /**
     * Scenario 2b — Invalid email format → HTTP 400.
     *
     * @Email on RegisterRequest.email triggers validation.
     * GlobalExceptionHandler returns HTTP 400 with the "email" field in the body.
     */
    @Test
    @DisplayName("POST /api/auth/register → 400 for an invalid email format")
    void register_invalidEmail_returns400() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Alice Smith");
        request.setEmail("not-a-valid-email");         // violates @Email
        request.setPassword("securePass1");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    /**
     * Scenario 2c — Password shorter than 8 characters → HTTP 400.
     *
     * @Size(min=8) on RegisterRequest.password triggers validation.
     * GlobalExceptionHandler returns HTTP 400 with the "password" field in the body.
     */
    @Test
    @DisplayName("POST /api/auth/register → 400 when password is shorter than 8 characters")
    void register_shortPassword_returns400() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Alice Smith");
        request.setEmail("alice@decisionhub.com");
        request.setPassword("short");                  // violates @Size(min=8)

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    /**
     * Scenario 3 — Duplicate email → HTTP 409 CONFLICT.
     *
     * AuthService.register() throws ResponseStatusException(CONFLICT).
     * Spring MVC's built-in ResponseStatusException handler returns HTTP 409 directly.
     * GlobalExceptionHandler does not handle ResponseStatusException.
     */
    @Test
    @DisplayName("POST /api/auth/register → 409 for a duplicate email")
    void register_duplicateEmail_returns409() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Alice Smith");
        request.setEmail("alice@decisionhub.com");
        request.setPassword("securePass1");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT, "Email is already registered"));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POST /api/auth/login
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Scenario 4 — Successful login → HTTP 200 OK.
     *
     * A valid LoginRequest is posted. AuthService.login() returns a LoginResponse
     * containing an access token. Controller wraps it in ResponseEntity.ok(...).
     * We assert the token value, tokenType ("Bearer"), and email in the response body.
     */
    @Test
    @DisplayName("POST /api/auth/login → 200 for valid credentials")
    void login_validCredentials_returns200() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("alice@decisionhub.com");
        request.setPassword("securePass1");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(stubLoginResponse());

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("alice@decisionhub.com"));
    }

    /**
     * Scenario 5a — Blank email → HTTP 400.
     *
     * @NotBlank on LoginRequest.email triggers validation.
     * GlobalExceptionHandler returns HTTP 400 with the "email" field in the body.
     */
    @Test
    @DisplayName("POST /api/auth/login → 400 when email is blank")
    void login_blankEmail_returns400() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("");                          // violates @NotBlank
        request.setPassword("securePass1");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    /**
     * Scenario 5b — Blank password → HTTP 400.
     *
     * @NotBlank on LoginRequest.password triggers validation.
     * GlobalExceptionHandler returns HTTP 400 with the "password" field in the body.
     */
    @Test
    @DisplayName("POST /api/auth/login → 400 when password is blank")
    void login_blankPassword_returns400() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("alice@decisionhub.com");
        request.setPassword("");                       // violates @NotBlank

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    /**
     * Scenario 6 — Wrong password → HTTP 500 INTERNAL SERVER ERROR.
     *
     * AuthService.login() propagates BadCredentialsException from
     * authenticationManager. GlobalExceptionHandler does NOT handle
     * BadCredentialsException, so Spring MVC returns HTTP 500.
     *
     * This test documents the ACTUAL current behaviour of the application.
     * Spring Security's HttpStatusEntryPoint(UNAUTHORIZED) in SecurityConfig
     * intercepts BadCredentialsException at the filter level and returns HTTP 401
     * before it can bubble up to GlobalExceptionHandler.
     * No production files are modified here.
     */
    @Test
    @DisplayName("POST /api/auth/login → 401 for wrong password (BadCredentialsException caught by Security entry point)")
    void login_wrongPassword_returns401() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("alice@decisionhub.com");
        request.setPassword("wrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Protected endpoint — unauthenticated access
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Scenario 7 — No token → HTTP 401 UNAUTHORIZED.
     *
     * SecurityConfig protects all routes except /api/auth/register and
     * /api/auth/login with .anyRequest().authenticated().
     * The authenticationEntryPoint is HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
     * so any unauthenticated request to a protected path returns exactly HTTP 401.
     *
     * We use a path not mapped by AuthController so the Security filter chain
     * rejects the request before it reaches any controller.
     */
    @Test
    @DisplayName("GET /api/users/me → 401 when no Authorization header is present")
    void protectedEndpoint_noToken_returns401() throws Exception {

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET /api/auth/me — protected, requires a valid JWT
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Scenario 10 — Valid JWT token → HTTP 200 OK with UserResponse body.
     *
     * The JwtAuthenticationFilter reads the Authorization header, calls
     * jwtService.extractUsername(), loads UserDetails, validates the token,
     * and sets the SecurityContext. AuthController.getCurrentUser() is then
     * reached and delegates to authService.getCurrentUser(email).
     */
    @Test
    @DisplayName("GET /api/auth/me → 200 for a valid JWT token")
    void getMe_validToken_returns200WithUserResponse() throws Exception {

        String token = "valid.jwt.token";
        String email = "alice@decisionhub.com";

        UserDetails stubUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("encodedHash")
                .roles("USER")
                .build();

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(stubUserDetails);
        when(jwtService.isTokenValid(token, email)).thenReturn(true);
        when(authService.getCurrentUser(email)).thenReturn(stubUserResponse());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    /**
     * Scenario 11 — No Authorization header on GET /api/auth/me → HTTP 401.
     *
     * JwtAuthenticationFilter sees a null authHeader and passes through
     * without setting the SecurityContext. Spring Security's
     * HttpStatusEntryPoint(UNAUTHORIZED) returns 401.
     */
    @Test
    @DisplayName("GET /api/auth/me → 401 when no Authorization header is provided")
    void getMe_noToken_returns401() throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Scenario 12 — Tampered or expired JWT → HTTP 401.
     *
     * JwtAuthenticationFilter catches JwtException from extractUsername(),
     * clears the SecurityContext, and continues the filter chain without
     * authentication. Spring Security denies the request with 401.
     */
    @Test
    @DisplayName("GET /api/auth/me → 401 when JWT token is tampered or expired")
    void getMe_tamperedOrExpiredToken_returns401() throws Exception {

        String badToken = "tampered.jwt.token";

        when(jwtService.extractUsername(badToken))
                .thenThrow(new JwtException("Token is invalid or expired"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + badToken))
                .andExpect(status().isUnauthorized());
    }

    // ══════════════════════════════════════════════════════════════════════
    // B3 Regression — BadCredentialsException returns 401 with JSON body
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Scenario 13 — Wrong password at login → HTTP 401 with JSON error body.
     *
     * Before fix B3, BadCredentialsException propagated as a 401 with an empty
     * body because GlobalExceptionHandler had no handler for it.
     * After fix B3, GlobalExceptionHandler catches it and returns:
     *   {"message":"Invalid email or password"}
     *
     * This test verifies both the status code AND the response body structure.
     */
    @Test
    @DisplayName("POST /api/auth/login → 401 with JSON body when credentials are invalid (B3 regression)")
    void login_badCredentials_returns401WithJsonBody() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("alice@decisionhub.com");
        request.setPassword("wrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException(
                        "Bad credentials"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
