package com.decisionhub.controller;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.UpdateDecisionRequest;
import com.decisionhub.entity.Decision;
import com.decisionhub.service.DecisionService;
import com.decisionhub.service.JwtService;
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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DecisionController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
@DisplayName("DecisionController tests")
class DecisionControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean DecisionService decisionService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean PasswordEncoder passwordEncoder;

    private Decision stubDecision() {
        com.decisionhub.entity.User creator = new com.decisionhub.entity.User();
        creator.setUserId(1L);
        creator.setEmail("alice@decisionhub.com");
        Decision d = new Decision();
        d.setDecisionId(1L);
        d.setTitle("Best Language?");
        d.setDescription("Vote on programming language");
        d.setPollType("SINGLE_CHOICE");
        d.setVisibility("PUBLIC");
        d.setStatus("OPEN");
        d.setCreatedBy(creator);
        d.setCreatedAt(LocalDateTime.now());
        return d;
    }

    @Test
    @DisplayName("POST /api/decisions → 201 for authenticated user")
    void createDecision_authenticated_returns201() throws Exception {
        when(decisionService.createDecision(anyString(), any(), any(), anyString(), anyString(), any()))
                .thenReturn(stubDecision());

        DecisionRequest req = new DecisionRequest();
        req.setTitle("Best Language?");
        req.setPollType("SINGLE_CHOICE");

        mockMvc.perform(post("/api/decisions")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Best Language?"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("POST /api/decisions → 400 when title is blank")
    void createDecision_blankTitle_returns400() throws Exception {
        DecisionRequest req = new DecisionRequest();
        req.setTitle("");
        req.setPollType("SINGLE_CHOICE");

        mockMvc.perform(post("/api/decisions")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/decisions → 401 when unauthenticated")
    void createDecision_unauthenticated_returns401() throws Exception {
        DecisionRequest req = new DecisionRequest();
        req.setTitle("Best Language?");
        req.setPollType("SINGLE_CHOICE");

        mockMvc.perform(post("/api/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/decisions → 200 with list")
    void getAllDecisions_returns200() throws Exception {
        when(decisionService.getAllDecisions()).thenReturn(List.of(stubDecision()));

        mockMvc.perform(get("/api/decisions")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/decisions/{id} → 200 for existing decision")
    void getDecisionById_found_returns200() throws Exception {
        when(decisionService.getDecisionById(1L)).thenReturn(stubDecision());

        mockMvc.perform(get("/api/decisions/1")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decisionId").value(1));
    }

    @Test
    @DisplayName("GET /api/decisions/{id} → 404 when not found")
    void getDecisionById_notFound_returns404() throws Exception {
        when(decisionService.getDecisionById(99L))
                .thenThrow(new IllegalArgumentException("Decision not found"));

        mockMvc.perform(get("/api/decisions/99")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/decisions/{id} → 200 for update (owner)")
    void updateDecision_owner_returns200() throws Exception {
        Decision updated = stubDecision();
        updated.setStatus("CLOSED");
        when(decisionService.updateDecision(eq(1L), any(), any(), anyString(), any(), any()))
                .thenReturn(updated);

        UpdateDecisionRequest req = new UpdateDecisionRequest();
        req.setStatus("CLOSED");

        mockMvc.perform(put("/api/decisions/1")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @DisplayName("PUT /api/decisions/{id} → 403 when non-owner attempts update (D1)")
    void updateDecision_nonOwner_returns403() throws Exception {
        when(decisionService.updateDecision(eq(1L), any(), any(), any(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not the owner of this decision"));

        UpdateDecisionRequest req = new UpdateDecisionRequest();
        req.setStatus("CLOSED");

        mockMvc.perform(put("/api/decisions/1")
                        .with(user("bob@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
