package com.decisionhub.controller;

import com.decisionhub.config.PasswordConfig;
import com.decisionhub.config.SecurityConfig;
import com.decisionhub.dto.ReportRequest;
import com.decisionhub.dto.UpdateReportRequest;
import com.decisionhub.entity.Report;
import com.decisionhub.entity.User;
import com.decisionhub.service.JwtService;
import com.decisionhub.service.ReportService;
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

@WebMvcTest(ReportController.class)
@Import({SecurityConfig.class, PasswordConfig.class})
@DisplayName("ReportController tests")
class ReportControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean ReportService reportService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean PasswordEncoder passwordEncoder;

    private Report stubReport() {
        User reporter = new User();
        reporter.setUserId(1L);
        reporter.setEmail("alice@decisionhub.com");
        Report r = new Report();
        r.setReportId(1L);
        r.setReportedBy(reporter);
        r.setTargetType("DECISION");
        r.setTargetId(5L);
        r.setReason("Spam content");
        r.setStatus("PENDING");
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    @Test
    @DisplayName("POST /api/reports → 201 for authenticated user")
    void createReport_authenticated_returns201() throws Exception {
        when(reportService.createReport(anyString(), anyLong(), anyString()))
                .thenReturn(stubReport());

        ReportRequest req = new ReportRequest();
        req.setTargetType("DECISION");
        req.setTargetId(5L);
        req.setReason("Spam content");

        mockMvc.perform(post("/api/reports")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/reports → 400 when reason is blank")
    void createReport_blankReason_returns400() throws Exception {
        ReportRequest req = new ReportRequest();
        req.setTargetType("DECISION");
        req.setTargetId(5L);
        req.setReason("");

        mockMvc.perform(post("/api/reports")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reports → 401 when unauthenticated")
    void createReport_unauthenticated_returns401() throws Exception {
        ReportRequest req = new ReportRequest();
        req.setTargetType("DECISION");
        req.setTargetId(5L);
        req.setReason("Spam");

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/admin/reports → 200 for ADMIN")
    void getAllReports_admin_returns200() throws Exception {
        when(reportService.getAllReports()).thenReturn(List.of(stubReport()));

        mockMvc.perform(get("/api/admin/reports")
                        .with(user("admin@decisionhub.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/admin/reports → 403 for USER role")
    void getAllReports_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/reports")
                        .with(user("alice@decisionhub.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/admin/reports/{id} → 200 for ADMIN updating status")
    void updateReport_admin_returns200() throws Exception {
        Report updated = stubReport();
        updated.setStatus("RESOLVED");
        when(reportService.updateReportStatus(eq(1L), eq("RESOLVED"))).thenReturn(updated);

        UpdateReportRequest req = new UpdateReportRequest();
        req.setStatus("RESOLVED");

        mockMvc.perform(patch("/api/admin/reports/1")
                        .with(user("admin@decisionhub.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("PATCH /api/admin/reports/{id} → 403 for USER role")
    void updateReport_userRole_returns403() throws Exception {
        UpdateReportRequest req = new UpdateReportRequest();
        req.setStatus("RESOLVED");

        mockMvc.perform(patch("/api/admin/reports/1")
                        .with(user("alice@decisionhub.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
