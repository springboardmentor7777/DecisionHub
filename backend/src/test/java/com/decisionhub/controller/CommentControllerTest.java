package com.decisionhub.controller;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.CommentRequest;
import com.decisionhub.entity.Comment;
import com.decisionhub.entity.Decision;
import com.decisionhub.entity.User;
import com.decisionhub.service.CommentService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
@DisplayName("CommentController tests")
class CommentControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean CommentService commentService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean PasswordEncoder passwordEncoder;

    private Comment stubComment() {
        User author = new User();
        author.setUserId(1L);
        author.setEmail("alice@decisionhub.com");
        author.setFullName("Alice Smith");
        Decision d = new Decision();
        d.setDecisionId(1L);
        Comment c = new Comment();
        c.setCommentId(1L);
        c.setUser(author);
        c.setDecision(d);
        c.setCommentText("Great idea!");
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/comments → 201 for valid comment")
    void addComment_valid_returns201() throws Exception {
        when(commentService.addComment(eq(1L), isNull(), anyString())).thenReturn(stubComment());

        CommentRequest req = new CommentRequest();
        req.setCommentText("Great idea!");

        mockMvc.perform(post("/api/decisions/1/comments")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentText").value("Great idea!"))
                .andExpect(jsonPath("$.userFullName").value("Alice Smith"));
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/comments → 400 when commentText blank")
    void addComment_blankText_returns400() throws Exception {
        CommentRequest req = new CommentRequest();
        req.setCommentText("");

        mockMvc.perform(post("/api/decisions/1/comments")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/decisions/{id}/comments → 401 when unauthenticated")
    void addComment_unauthenticated_returns401() throws Exception {
        CommentRequest req = new CommentRequest();
        req.setCommentText("Hello");

        mockMvc.perform(post("/api/decisions/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/decisions/{id}/comments → 200 with list")
    void getComments_returns200() throws Exception {
        when(commentService.getComments(1L)).thenReturn(List.of(stubComment()));

        mockMvc.perform(get("/api/decisions/1/comments")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userFullName").value("Alice Smith"));
    }
}
