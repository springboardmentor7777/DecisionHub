package com.decisionhub.controller;

import com.decisionhub.dto.ReportRequest;
import com.decisionhub.dto.ReportResponse;
import com.decisionhub.dto.UpdateReportRequest;
import com.decisionhub.entity.Report;
import com.decisionhub.service.ReportService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // POST /api/reports — any authenticated user
    @PostMapping("/api/reports")
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportRequest request) {

        Report report = reportService.createReport(
                request.getTargetType(),
                request.getTargetId(),
                request.getReason());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(report));
    }

    // GET /api/admin/reports — ADMIN only
    @GetMapping("/api/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        List<ReportResponse> list = reportService.getAllReports()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // PATCH /api/admin/reports/{id} — ADMIN only
    @PatchMapping("/api/admin/reports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportRequest request) {

        Report report = reportService.updateReportStatus(id, request.getStatus());
        return ResponseEntity.ok(toResponse(report));
    }

    private ReportResponse toResponse(Report r) {
        return new ReportResponse(
                r.getReportId(),
                r.getTargetType(),
                r.getTargetId(),
                r.getReason(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getReportedBy() != null ? r.getReportedBy().getEmail() : null);
    }
}
