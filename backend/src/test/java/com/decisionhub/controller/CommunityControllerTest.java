package com.decisionhub.controller;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.CommunityRequest;
import com.decisionhub.dto.CommunityResponse;
import com.decisionhub.service.CommunityService;
import com.decisionhub.service.JwtService;
import com.decisionhub.entity.Community;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommunityController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
@DisplayName("CommunityController tests")
class CommunityControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean CommunityService communityService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean PasswordEncoder passwordEncoder;

    private Community stubCommunity() {
        com.decisionhub.entity.User creator = new com.decisionhub.entity.User();
        creator.setUserId(1L);
        creator.setEmail("alice@decisionhub.com");
        Community c = new Community();
        c.setCommunityId(1L);
        c.setCommunityName("Test Board");
        c.setDescription("A test community");
        c.setCreatedBy(creator);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }

    @Test
    @DisplayName("POST /api/communities → 201 for authenticated user")
    void createCommunity_authenticated_returns201() throws Exception {
        when(communityService.createCommunity(anyString(), anyString()))
                .thenReturn(stubCommunity());

        CommunityRequest req = new CommunityRequest();
        req.setCommunityName("Test Board");
        req.setDescription("A test community");

        mockMvc.perform(post("/api/communities")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.communityName").value("Test Board"));
    }

    @Test
    @DisplayName("POST /api/communities → 400 when communityName is blank")
    void createCommunity_blankName_returns400() throws Exception {
        CommunityRequest req = new CommunityRequest();
        req.setCommunityName("");

        mockMvc.perform(post("/api/communities")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/communities → 401 when unauthenticated")
    void createCommunity_unauthenticated_returns401() throws Exception {
        CommunityRequest req = new CommunityRequest();
        req.setCommunityName("Test Board");

        mockMvc.perform(post("/api/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/communities → 200 with list")
    void getAllCommunities_returns200() throws Exception {
        when(communityService.getAllCommunities()).thenReturn(List.of(stubCommunity()));

        mockMvc.perform(get("/api/communities")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/communities/{id} → 200 for existing community")
    void getCommunityById_found_returns200() throws Exception {
        when(communityService.getCommunityById(1L)).thenReturn(stubCommunity());

        mockMvc.perform(get("/api/communities/1")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communityId").value(1));
    }

    @Test
    @DisplayName("GET /api/communities/{id} → 404 when not found")
    void getCommunityById_notFound_returns404() throws Exception {
        when(communityService.getCommunityById(99L))
                .thenThrow(new IllegalArgumentException("Community not found"));

        mockMvc.perform(get("/api/communities/99")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isNotFound());
    }
}
