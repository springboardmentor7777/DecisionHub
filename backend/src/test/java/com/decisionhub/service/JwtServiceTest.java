package com.decisionhub.service;

import io.jsonwebtoken.JwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService.
 *
 * No Spring context is loaded — JwtService is instantiated directly and
 * its @Value-injected fields (secret, expiration) are set via
 * ReflectionTestUtils.setField().
 *
 * No database connection is required.
 */
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_EMAIL      = "test@decisionhub.com";
    private static final long   TEST_EXPIRATION = 3_600_000L; // 1 hour in ms

    /**
     * A Base64-encoded secret that decodes to 40 bytes (320 bits).
     * HMAC-SHA256 requires a minimum of 256 bits (32 bytes).
     * Computed at runtime so the value is self-documenting.
     */
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "DecisionHubJwtSecretKeyForTestingOnly123"
                    .getBytes(StandardCharsets.UTF_8)
    );

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",     TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION);
    }

    // -----------------------------------------------------------------------
    // Scenario 1: Token generation returns a non-empty token
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("generateToken() returns a non-null, non-empty JWT string")
    void generateToken_returnsNonEmptyToken() {
        String token = jwtService.generateToken(TEST_EMAIL);

        assertNotNull(token, "Token must not be null");
        assertFalse(token.isBlank(), "Token must not be blank");
        // A compact JWT always contains exactly two dots (header.payload.signature)
        assertEquals(2, token.chars().filter(c -> c == '.').count(),
                "Token must have three segments separated by two dots");
    }

    // -----------------------------------------------------------------------
    // Scenario 2: Extracting username returns the correct email
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("extractUsername() returns the email used to generate the token")
    void extractUsername_returnsCorrectEmail() {
        String token     = jwtService.generateToken(TEST_EMAIL);
        String extracted = jwtService.extractUsername(token);

        assertEquals(TEST_EMAIL, extracted,
                "Extracted email must match the one used to generate the token");
    }

    // -----------------------------------------------------------------------
    // Scenario 3: A valid token with the correct email is valid
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("isTokenValid() returns true for a fresh token with the correct email")
    void isTokenValid_correctEmail_returnsTrue() {
        String token = jwtService.generateToken(TEST_EMAIL);

        assertTrue(jwtService.isTokenValid(token, TEST_EMAIL),
                "A fresh token with the correct email must be valid");
    }

    // -----------------------------------------------------------------------
    // Scenario 4: A valid token with a different email is NOT valid
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("isTokenValid() returns false when the email does not match the token subject")
    void isTokenValid_wrongEmail_returnsFalse() {
        String token = jwtService.generateToken(TEST_EMAIL);

        assertFalse(jwtService.isTokenValid(token, "other@example.com"),
                "A token generated for one email must not be valid for a different email");
    }

    // -----------------------------------------------------------------------
    // Scenario 5: An expired token throws JwtException
    //
    // Note: isTokenValid() does not catch exceptions internally.
    // When expiration < 0, the token is already expired at creation time.
    // JJWT's parseSignedClaims() throws ExpiredJwtException (a JwtException
    // subclass) during parsing — it does NOT return false.
    // JwtAuthenticationFilter catches this exception externally.
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("isTokenValid() throws JwtException for an already-expired token")
    void isTokenValid_expiredToken_throwsJwtException() {
        // Override expiration to -1000ms so the token is expired at creation
        ReflectionTestUtils.setField(jwtService, "expiration", -1_000L);
        String expiredToken = jwtService.generateToken(TEST_EMAIL);

        // Restore normal expiration before asserting (keeps test isolation clean)
        ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION);

        assertThrows(JwtException.class,
                () -> jwtService.isTokenValid(expiredToken, TEST_EMAIL),
                "An expired token must cause JwtException to be thrown");
    }

    // -----------------------------------------------------------------------
    // Scenario 6: A tampered token throws JwtException
    //
    // Corrupting the signature segment causes JJWT to throw SignatureException
    // (a JwtException subclass) — the token is rejected outright.
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("isTokenValid() throws JwtException when the token signature is tampered")
    void isTokenValid_tamperedToken_throwsJwtException() {
        String token = jwtService.generateToken(TEST_EMAIL);

        // Replace the last 6 characters of the signature with garbage
        String tamperedToken = token.substring(0, token.length() - 6) + "XXXXXX";

        assertThrows(JwtException.class,
                () -> jwtService.isTokenValid(tamperedToken, TEST_EMAIL),
                "A token with a corrupted signature must cause JwtException to be thrown");
    }
}
