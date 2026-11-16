package com.decisionhub.controller;

import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.UpdateDecisionRequest;
import com.decisionhub.entity.Decision;
import com.decisionhub.service.DecisionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    // POST /api/decisions
    @PostMapping
    public ResponseEntity<DecisionResponse> createDecision(
            @Valid @RequestBody DecisionRequest request) {

        LocalDateTime endsAt = parseDateTime(request.getEndsAt());

        Decision decision = decisionService.createDecision(
                request.getTitle(),
                request.getDescription(),
                request.getCommunityId(),
                request.getPollType(),
                request.getVisibility(),
                endsAt);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(decision));
    }

    // GET /api/decisions
    @GetMapping
    public ResponseEntity<List<DecisionResponse>> getAllDecisions() {
        List<DecisionResponse> list = decisionService.getAllDecisions()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // GET /api/decisions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DecisionResponse> getDecisionById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(decisionService.getDecisionById(id)));
    }

    // PUT /api/decisions/{id}
    @PutMapping("/{id}")
    public ResponseEntity<DecisionResponse> updateDecision(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDecisionRequest request) {

        LocalDateTime endsAt = parseDateTime(request.getEndsAt());

        Decision decision = decisionService.updateDecision(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getVisibility(),
                endsAt);

        return ResponseEntity.ok(toResponse(decision));
    }

    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDateTime.parse(raw);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date/time format. Use ISO-8601 (e.g. 2026-12-31T23:59:59)");
        }
    }

    private DecisionResponse toResponse(Decision d) {
        return new DecisionResponse(
                d.getDecisionId(),
                d.getTitle(),
                d.getDescription(),
                d.getCommunity() != null ? d.getCommunity().getCommunityId() : null,
                d.getPollType(),
                d.getVisibility(),
                d.getStatus(),
                d.getEndsAt(),
                d.getCreatedAt(),
                d.getCreatedBy() != null ? d.getCreatedBy().getEmail() : null);
    }
}
