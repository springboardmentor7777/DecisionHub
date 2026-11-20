package com.decisionhub.security;

import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Manually inject the @Value properties because we are not starting Spring
        ReflectionTestUtils.setField(jwtService, "secret", "TEST_SECRET_KEY_MUST_BE_LONG_ENOUGH_1234567890");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);
    }

    @Test
    void generateAccessToken_shouldCreateValidToken() {
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashed_password")
                .role(Role.USER)
                .build();

        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotEmpty();
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void generateRefreshToken_shouldCreateValidToken() {
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashed_password")
                .role(Role.ADMINISTRATOR)
                .build();

        String token = jwtService.generateRefreshToken(user);

        assertThat(token).isNotEmpty();
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {
        User user1 = User.builder()
                .email("user1@example.com")
                .passwordHash("hashed")
                .role(Role.USER)
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .passwordHash("hashed")
                .role(Role.USER)
                .build();

        String token = jwtService.generateAccessToken(user1);

        // Token generated for user1 should NOT be valid for user2
        assertThat(jwtService.isTokenValid(token, user2)).isFalse();
    }
}