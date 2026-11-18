package com.decisionhub.controller;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.VoteRequest;
import com.decisionhub.entity.Decision;
import com.decisionhub.entity.Option;
import com.decisionhub.entity.Vote;
import com.decisionhub.service.JwtService;
import com.decisionhub.service.VoteService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoteController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
@DisplayName("VoteController tests")
class VoteControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean VoteService voteService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean PasswordEncoder passwordEncoder;

    private Vote stubVote() {
        Decision d = new Decision();
        d.setDecisionId(1L);
        Option o = new Option();
        o.setOptionId(10L);
        o.setDecision(d);
        Vote v = new Vote();
        v.setVoteId(1L);
        v.setDecision(d);
        v.setOption(o);
        v.setVotedAt(LocalDateTime.now());
        return v;
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/votes → 201 for first vote")
    void castVote_firstVote_returns201() throws Exception {
        when(voteService.castVote(eq(1L), eq(10L), isNull())).thenReturn(stubVote());

        VoteRequest req = new VoteRequest();
        req.setOptionId(10L);

        mockMvc.perform(post("/api/decisions/1/votes")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voteId").value(1));
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/votes → 409 for duplicate vote")
    void castVote_duplicateVote_returns409() throws Exception {
        when(voteService.castVote(eq(1L), any(), any()))
                .thenThrow(new IllegalStateException("User has already voted on this decision"));

        VoteRequest req = new VoteRequest();
        req.setOptionId(10L);

        mockMvc.perform(post("/api/decisions/1/votes")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User has already voted on this decision"));
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/votes → 401 when unauthenticated")
    void castVote_unauthenticated_returns401() throws Exception {
        VoteRequest req = new VoteRequest();
        req.setOptionId(10L);

        mockMvc.perform(post("/api/decisions/1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/votes → 404 when decision not found")
    void castVote_decisionNotFound_returns404() throws Exception {
        when(voteService.castVote(eq(99L), any(), any()))
                .thenThrow(new IllegalArgumentException("Decision not found"));

        VoteRequest req = new VoteRequest();
        req.setOptionId(10L);

        mockMvc.perform(post("/api/decisions/99/votes")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/votes → 201 for RATING poll with rating payload")
    void castVote_ratingPayload_returns201() throws Exception {
        // Build a stub Vote with no option (RATING poll)
        Decision d = new Decision();
        d.setDecisionId(3L);
        Vote ratingVote = new Vote();
        ratingVote.setVoteId(5L);
        ratingVote.setDecision(d);
        ratingVote.setOption(null);   // RATING: no option
        ratingVote.setRating(4);
        ratingVote.setVotedAt(java.time.LocalDateTime.now());

        when(voteService.castVote(eq(3L), isNull(), eq(4))).thenReturn(ratingVote);

        VoteRequest req = new VoteRequest();
        req.setRating(4);             // optionId intentionally omitted (null)

        mockMvc.perform(post("/api/decisions/3/votes")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voteId").value(5))
                .andExpect(jsonPath("$.rating").value(4));
    }
}
