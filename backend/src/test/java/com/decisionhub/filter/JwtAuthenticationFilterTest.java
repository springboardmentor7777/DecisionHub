package com.decisionhub.filter;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.service.AuthService;
import com.decisionhub.service.JwtService;
import com.decisionhub.controller.AuthController;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.jsonwebtoken.JwtException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for JwtAuthenticationFilter.
 *
 * Uses @WebMvcTest(AuthController.class) so Security IS active.
 * We exercise /api/auth/me (a protected endpoint) to observe filter behaviour.
 *
 * Five scenarios:
 *   1. Valid Bearer token  → filter sets SecurityContext → 200
 *   2. Expired token       → JwtException thrown by extractUsername → 401
 *   3. Tampered token      → JwtException thrown by extractUsername → 401
 *   4. Non-Bearer header   → filter skips JWT processing → 401 (unauthenticated)
 *   5. No Authorization header → filter skips JWT processing → 401 (unauthenticated)
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
@DisplayName("JwtAuthenticationFilter – integration slice tests")
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    // -----------------------------------------------------------------------
    // Scenario 1 – valid token → 200
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Valid Bearer token: filter sets SecurityContext and request reaches controller (200)")
    void request_withValidToken_setsSecurityContext() throws Exception {

        String validToken = "valid.jwt.token";
        String email      = "alice@example.com";

        UserDetails userDetails = new User(
                email, "hashed",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        UserResponse userResponse = new UserResponse(
                1L, "alice", email, "USER",
                LocalDateTime.of(2024, 1, 1, 0, 0));

        when(jwtService.extractUsername(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, email)).thenReturn(true);
        when(authService.getCurrentUser(any())).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // Scenario 2 – expired token → JwtException → 401
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Expired token: extractUsername throws JwtException → 401")
    void request_withExpiredToken_returns401() throws Exception {

        String expiredToken = "expired.jwt.token";

        when(jwtService.extractUsername(expiredToken))
                .thenThrow(new JwtException("JWT expired"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Scenario 3 – tampered token → JwtException → 401
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Tampered token: extractUsername throws JwtException → 401")
    void request_withTamperedToken_returns401() throws Exception {

        String tamperedToken = "tampered.jwt.token";

        when(jwtService.extractUsername(tamperedToken))
                .thenThrow(new JwtException("JWT signature does not match"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Scenario 4 – non-Bearer Authorization header → filter skips → 401
    // Note: extractUsername is NEVER called; no mock needed.
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Non-Bearer Authorization header: filter passes through unauthenticated → 401")
    void request_withNonBearerHeader_returns401() throws Exception {

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Scenario 5 – no Authorization header → filter skips → 401
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("No Authorization header: request reaches security gate unauthenticated → 401")
    void request_withNoHeader_returns401() throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
