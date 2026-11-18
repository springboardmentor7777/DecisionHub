package com.decisionhub.controller;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.UpdateUserRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.service.JwtService;
import com.decisionhub.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
@DisplayName("UserController tests")
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean PasswordEncoder passwordEncoder;

    private UserResponse stubUser() {
        return new UserResponse(1L, "Alice Smith", "alice@decisionhub.com",
                "USER", LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    @Test
    @DisplayName("GET /api/users/{id} → 200 for existing user")
    void getUserById_found_returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(stubUser());

        mockMvc.perform(get("/api/users/1")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("alice@decisionhub.com"));
    }

    @Test
    @DisplayName("GET /api/users/{id} → 401 when unauthenticated")
    void getUserById_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/{id} → 404 when not found")
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/users/99")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/users/{id} → 200 when user updates own profile")
    void updateUser_ownProfile_returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(stubUser());
        when(userService.updateUser(eq(1L), anyString()))
                .thenReturn(new UserResponse(1L, "Alice Updated", "alice@decisionhub.com",
                        "USER", LocalDateTime.now()));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFullName("Alice Updated");

        mockMvc.perform(put("/api/users/1")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Alice Updated"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} → 403 when user updates another user's profile")
    void updateUser_otherProfile_returns403() throws Exception {
        UserResponse otherUser = new UserResponse(2L, "Bob", "bob@decisionhub.com",
                "USER", LocalDateTime.now());
        when(userService.getUserById(2L)).thenReturn(otherUser);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFullName("Hacked Name");

        mockMvc.perform(put("/api/users/2")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
