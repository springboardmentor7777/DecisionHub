package com.decisionhub.controller;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.entity.Notification;
import com.decisionhub.entity.User;
import com.decisionhub.service.JwtService;
import com.decisionhub.service.NotificationService;
import com.decisionhub.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
@DisplayName("NotificationController tests")
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean NotificationService notificationService;
    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean PasswordEncoder passwordEncoder;

    private Notification stubNotification() {
        User u = new User();
        u.setUserId(1L);
        u.setEmail("alice@decisionhub.com");
        Notification n = new Notification();
        n.setNotificationId(1L);
        n.setUser(u);
        n.setMessage("New vote on your decision");
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    @Test
    @DisplayName("GET /api/notifications → 200 with list for authenticated user")
    void getNotifications_authenticated_returns200() throws Exception {
        when(userService.getUserByEmail("alice@decisionhub.com"))
                .thenReturn(new UserResponse(1L, "Alice", "alice@decisionhub.com",
                        "USER", LocalDateTime.now()));
        when(notificationService.getNotifications(1L))
                .thenReturn(List.of(stubNotification()));

        mockMvc.perform(get("/api/notifications")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message").value("New vote on your decision"));
    }

    @Test
    @DisplayName("GET /api/notifications → 401 when unauthenticated")
    void getNotifications_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read → 200 marks as read (owner)")
    void markAsRead_owner_returns200() throws Exception {
        Notification read = stubNotification();
        read.setIsRead(true);
        // D2: service now takes (id, ownerEmail) — matcher uses anyString() for email
        when(notificationService.markAsRead(eq(1L), eq("alice@decisionhub.com")))
                .thenReturn(read);

        mockMvc.perform(patch("/api/notifications/1/read")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read → 404 when notification not found")
    void markAsRead_notFound_returns404() throws Exception {
        when(notificationService.markAsRead(eq(99L), anyString()))
                .thenThrow(new RuntimeException("Notification not found"));

        mockMvc.perform(patch("/api/notifications/99/read")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read → 403 when non-owner attempts markAsRead (D2)")
    void markAsRead_nonOwner_returns403() throws Exception {
        // Bob tries to mark Alice's notification — service throws FORBIDDEN
        when(notificationService.markAsRead(eq(1L), eq("bob@decisionhub.com")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not allowed to mark this notification as read"));

        mockMvc.perform(patch("/api/notifications/1/read")
                        .with(user("bob@decisionhub.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read → 401 when unauthenticated")
    void markAsRead_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().isUnauthorized());
    }
}
