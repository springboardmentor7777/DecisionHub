package com.decisionhub.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generatesAndValidatesToken() {
        UserDetails user = User.builder()
                .username("test@example.com")
                .password("irrelevant")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateAccessToken(user);
        assertThat(token).isNotEmpty();

        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }
}