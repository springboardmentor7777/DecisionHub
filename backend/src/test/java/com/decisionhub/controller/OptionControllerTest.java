package com.decisionhub.controller;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.OptionRequest;
import com.decisionhub.dto.OptionResponse;
import com.decisionhub.entity.Decision;
import com.decisionhub.entity.Option;
import com.decisionhub.service.JwtService;
import com.decisionhub.service.OptionService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OptionController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
@DisplayName("OptionController tests")
class OptionControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean OptionService optionService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean PasswordEncoder passwordEncoder;

    /** Raw entity stub — used by POST path which calls addOption() then toResponse(). */
    private Option stubOption(Long optionId, Long decisionId) {
        Decision d = new Decision();
        d.setDecisionId(decisionId);
        Option o = new Option();
        o.setOptionId(optionId);
        o.setOptionText("Option A");
        o.setDecision(d);
        o.setCreatedAt(LocalDateTime.now());
        return o;
    }

    /** DTO stub — used by GET path which returns List<OptionResponse> directly. */
    private OptionResponse stubResponse(Long optionId, Long decisionId, long voteCount) {
        return new OptionResponse(optionId, "Option A", decisionId, LocalDateTime.now(), voteCount);
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/decisions/{id}/options → 201 with voteCount=0 for new option")
    void addOption_authenticated_returns201() throws Exception {
        Option opt = stubOption(1L, 1L);
        when(optionService.addOption(eq(1L), anyString())).thenReturn(opt);
        when(optionService.getVoteCount(1L)).thenReturn(0L);

        OptionRequest req = new OptionRequest();
        req.setOptionText("Option A");

        mockMvc.perform(post("/api/decisions/1/options")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.optionText").value("Option A"))
                .andExpect(jsonPath("$.voteCount").value(0));
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/options → 400 when optionText blank")
    void addOption_blankText_returns400() throws Exception {
        OptionRequest req = new OptionRequest();
        req.setOptionText("");

        mockMvc.perform(post("/api/decisions/1/options")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/options → 401 when unauthenticated")
    void addOption_unauthenticated_returns401() throws Exception {
        OptionRequest req = new OptionRequest();
        req.setOptionText("Option A");

        mockMvc.perform(post("/api/decisions/1/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── GET ──────────────────────────────────────────────────────────────────
    // GET path calls getOptionsWithVoteCounts() which returns List<OptionResponse>
    // directly — bulk-count path, no per-option getVoteCount() call.

    @Test
    @DisplayName("GET /api/decisions/{id}/options → 200 with voteCount populated (3 votes)")
    void getOptions_returns200WithVoteCount() throws Exception {
        when(optionService.getOptionsWithVoteCounts(1L))
                .thenReturn(List.of(stubResponse(1L, 1L, 3L)));

        mockMvc.perform(get("/api/decisions/1/options")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].voteCount").value(3));
    }

    @Test
    @DisplayName("GET /api/decisions/{id}/options → 200 with voteCount=0 when no votes")
    void getOptions_returns200WithZeroVoteCount() throws Exception {
        when(optionService.getOptionsWithVoteCounts(1L))
                .thenReturn(List.of(stubResponse(1L, 1L, 0L)));

        mockMvc.perform(get("/api/decisions/1/options")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].voteCount").value(0));
    }

    @Test
    @DisplayName("GET /api/decisions/{id}/options → 200 with multiple options, different vote counts")
    void getOptions_multipleOptions_differentVoteCounts() throws Exception {
        List<OptionResponse> responses = List.of(
                new OptionResponse(1L, "Option A", 1L, LocalDateTime.now(), 5L),
                new OptionResponse(2L, "Option B", 1L, LocalDateTime.now(), 2L),
                new OptionResponse(3L, "Option C", 1L, LocalDateTime.now(), 0L)
        );
        when(optionService.getOptionsWithVoteCounts(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/decisions/1/options")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].voteCount").value(5))
                .andExpect(jsonPath("$[1].voteCount").value(2))
                .andExpect(jsonPath("$[2].voteCount").value(0));
    }

    @Test
    @DisplayName("GET /api/decisions/{id}/options → 404 when decision not found")
    void getOptions_decisionNotFound_returns404() throws Exception {
        when(optionService.getOptionsWithVoteCounts(99L))
                .thenThrow(new IllegalArgumentException("Decision not found"));

        mockMvc.perform(get("/api/decisions/99/options")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isNotFound());
    }
}
