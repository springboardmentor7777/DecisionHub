package com.decisionhub.service;

import com.decisionhub.entity.Report;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.repository.ReportRepository;
import com.decisionhub.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportService.
 *
 * Tests:
 *   createReport() — 4 scenarios
 *     1. Success: authenticated user found → report saved and returned
 *     2. Field mapping: reportedBy, targetType, targetId, reason, status, createdAt
 *     3. Status defaults to "PENDING"
 *     4. User not found → RuntimeException("User not found")
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService unit tests")
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private UserRepository   userRepository;

    @InjectMocks
    private ReportService reportService;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private static final String EMAIL = "alice@decisionhub.com";

    private User reporter;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName("USER");

        reporter = new User();
        reporter.setUserId(1L);
        reporter.setEmail(EMAIL);
        reporter.setRole(role);
    }

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                email, null, Collections.emptyList()));
        SecurityContextHolder.setContext(ctx);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 1 — Success
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createReport() saves and returns Report when user is authenticated and found")
    void createReport_authenticatedUser_savesAndReturnsReport() {

        authenticateAs(EMAIL);

        Report saved = new Report();
        saved.setReportId(1L);
        saved.setReportedBy(reporter);
        saved.setTargetType("COMMENT");
        saved.setTargetId(42L);
        saved.setReason("Spam");
        saved.setStatus("PENDING");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(reporter));
        when(reportRepository.save(any(Report.class))).thenReturn(saved);

        Report result = reportService.createReport("COMMENT", 42L, "Spam");

        assertNotNull(result);
        assertEquals(1L, result.getReportId());
        assertEquals("COMMENT", result.getTargetType());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 2 — Field mapping
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createReport() correctly maps reportedBy, targetType, targetId, reason, status and createdAt")
    void createReport_fieldsMappedCorrectly() {

        authenticateAs(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(reporter));
        when(reportRepository.save(any(Report.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        reportService.createReport("DECISION", 10L, "Inappropriate content");

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(captor.capture());

        Report captured = captor.getValue();
        assertEquals(reporter,                 captured.getReportedBy());
        assertEquals("DECISION",               captured.getTargetType());
        assertEquals(10L,                      captured.getTargetId());
        assertEquals("Inappropriate content",  captured.getReason());
        assertEquals("PENDING",                captured.getStatus());
        assertNotNull(captured.getCreatedAt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 3 — Default status is PENDING
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createReport() always sets status to PENDING regardless of input")
    void createReport_statusDefaultsPending() {

        authenticateAs(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(reporter));
        when(reportRepository.save(any(Report.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Report result = reportService.createReport("USER", 7L, "Harassment");

        assertEquals("PENDING", result.getStatus());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 4 — User not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createReport() throws RuntimeException when authenticated user absent in DB")
    void createReport_userNotFound_throwsRuntimeException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reportService.createReport("COMMENT", 1L, "Spam"));

        assertEquals("User not found", ex.getMessage());
        verify(reportRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 5 — getAllReports() returns list ordered by createdAt desc
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllReports() returns all reports ordered by createdAt descending")
    void getAllReports_returnsList() {

        Report r1 = new Report();
        r1.setReportId(1L);
        r1.setReportedBy(reporter);
        r1.setTargetType("DECISION");
        r1.setTargetId(10L);
        r1.setReason("Spam");
        r1.setStatus("PENDING");
        r1.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));

        Report r2 = new Report();
        r2.setReportId(2L);
        r2.setReportedBy(reporter);
        r2.setTargetType("COMMENT");
        r2.setTargetId(5L);
        r2.setReason("Offensive");
        r2.setStatus("REVIEWED");
        r2.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 9, 0));

        // Repository already returns in descending order (newest first)
        when(reportRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(r1, r2));

        List<Report> result = reportService.getAllReports();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getReportId());   // r1 is newer → first
        assertEquals(2L, result.get(1).getReportId());
        verify(reportRepository).findAllByOrderByCreatedAtDesc();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 6 — updateReportStatus() sets new status
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateReportStatus() persists and returns the report with the updated status")
    void updateReportStatus_setsNewStatus() {

        Report existing = new Report();
        existing.setReportId(1L);
        existing.setReportedBy(reporter);
        existing.setTargetType("DECISION");
        existing.setTargetId(10L);
        existing.setReason("Spam");
        existing.setStatus("PENDING");
        existing.setCreatedAt(java.time.LocalDateTime.now());

        when(reportRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reportRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        Report result = reportService.updateReportStatus(1L, "RESOLVED");

        assertEquals("RESOLVED", result.getStatus());
        verify(reportRepository).save(existing);
    }

    @Test
    @DisplayName("updateReportStatus() throws IllegalArgumentException when report not found")
    void updateReportStatus_reportNotFound_throwsIllegalArgumentException() {

        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reportService.updateReportStatus(999L, "RESOLVED"));

        assertEquals("Report not found", ex.getMessage());
        verify(reportRepository, never()).save(any());
    }
}
