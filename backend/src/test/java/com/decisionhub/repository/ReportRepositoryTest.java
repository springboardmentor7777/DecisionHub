package com.decisionhub.repository;

import com.decisionhub.entity.Report;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slice test for ReportRepository using @DataJpaTest (H2 in-memory).
 *
 * Tests:
 *   1. save() persists a Report with status PENDING and assigns a generated reportId
 *   2. findById() returns the Report when it exists
 *   3. findAll() returns all saved reports
 *   4. save() with null reason throws DataIntegrityViolationException
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("ReportRepository slice tests")
class ReportRepositoryTest {

    @Autowired private ReportRepository reportRepository;
    @Autowired private UserRepository   userRepository;
    @Autowired private RoleRepository   roleRepository;

    private User reporter;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("USER");
        role = roleRepository.save(role);

        reporter = new User();
        reporter.setFullName("Alice Smith");
        reporter.setEmail("alice@decisionhub.com");
        reporter.setPasswordHash("hash");
        reporter.setRole(role);
        reporter.setCreatedAt(LocalDateTime.now());
        reporter = userRepository.save(reporter);
    }

    private Report buildReport(String targetType, Long targetId, String reason) {
        Report r = new Report();
        r.setReportedBy(reporter);
        r.setTargetType(targetType);
        r.setTargetId(targetId);
        r.setReason(reason);
        r.setStatus("PENDING");
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 1 — save() assigns reportId and sets PENDING status
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() persists a Report with status PENDING and assigns a generated reportId")
    void save_validReport_assignsIdAndStatus() {

        Report saved = reportRepository.save(buildReport("COMMENT", 42L, "Spam content"));

        assertNotNull(saved.getReportId());
        assertEquals("COMMENT",      saved.getTargetType());
        assertEquals(42L,             saved.getTargetId());
        assertEquals("Spam content", saved.getReason());
        assertEquals("PENDING",       saved.getStatus());
        assertEquals(reporter.getUserId(), saved.getReportedBy().getUserId());
        assertNotNull(saved.getCreatedAt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 2 — findById() found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findById() returns the Report when it exists")
    void findById_existingId_returnsReport() {

        Report saved = reportRepository.save(buildReport("DECISION", 10L, "Inappropriate"));

        Optional<Report> result = reportRepository.findById(saved.getReportId());

        assertTrue(result.isPresent());
        assertEquals("DECISION",      result.get().getTargetType());
        assertEquals("Inappropriate", result.get().getReason());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 3 — findAll() returns all reports
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findAll() returns all persisted reports")
    void findAll_returnsAllReports() {

        reportRepository.save(buildReport("COMMENT",  1L, "Reason 1"));
        reportRepository.save(buildReport("DECISION", 2L, "Reason 2"));
        reportRepository.save(buildReport("USER",     3L, "Reason 3"));

        List<Report> all = reportRepository.findAll();

        assertEquals(3, all.size());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 4 — Null reason violates NOT NULL constraint
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() with null reason throws DataIntegrityViolationException")
    void save_nullReason_throwsDataIntegrityViolationException() {

        assertThrows(DataIntegrityViolationException.class, () -> {
            reportRepository.save(buildReport("COMMENT", 1L, null));
            reportRepository.flush();
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 5 — findAllByOrderByCreatedAtDesc() returns reports newest-first
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findAllByOrderByCreatedAtDesc() returns reports ordered by createdAt descending")
    void findAllByOrderByCreatedAtDesc_returnsNewestFirst() {

        // Save three reports with distinct timestamps
        Report oldest = buildReport("DECISION", 1L, "Oldest reason");
        oldest.setCreatedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        reportRepository.save(oldest);

        Report middle = buildReport("COMMENT", 2L, "Middle reason");
        middle.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        reportRepository.save(middle);

        Report newest = buildReport("USER", 3L, "Newest reason");
        newest.setCreatedAt(LocalDateTime.of(2026, 1, 1, 11, 0));
        reportRepository.save(newest);

        reportRepository.flush();

        List<Report> result = reportRepository.findAllByOrderByCreatedAtDesc();

        assertEquals(3, result.size());
        assertEquals("Newest reason", result.get(0).getReason());   // newest first
        assertEquals("Middle reason",  result.get(1).getReason());
        assertEquals("Oldest reason",  result.get(2).getReason());
    }
}
