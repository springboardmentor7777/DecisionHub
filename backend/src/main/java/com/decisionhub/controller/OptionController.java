package com.decisionhub.controller;

import com.decisionhub.dto.OptionRequest;
import com.decisionhub.dto.OptionResponse;
import com.decisionhub.entity.Option;
import com.decisionhub.service.OptionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decisions/{decisionId}/options")
public class OptionController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    // POST /api/decisions/{decisionId}/options
    @PostMapping
    public ResponseEntity<OptionResponse> addOption(
            @PathVariable Long decisionId,
            @Valid @RequestBody OptionRequest request) {

        Option option = optionService.addOption(decisionId, request.getOptionText());
        // Single-option path: one COUNT query is acceptable here
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(option));
    }

    // GET /api/decisions/{decisionId}/options
    // Uses the bulk-count service method: 2 queries total (fetch options + 1 GROUP BY)
    // regardless of the number of options — no N+1 problem.
    @GetMapping
    public ResponseEntity<List<OptionResponse>> getOptions(@PathVariable Long decisionId) {
        return ResponseEntity.ok(optionService.getOptionsWithVoteCounts(decisionId));
    }

    /** Used only by the POST path (single newly created option). */
    private OptionResponse toResponse(Option o) {
        long voteCount = optionService.getVoteCount(o.getOptionId());
        return new OptionResponse(
                o.getOptionId(),
                o.getOptionText(),
                o.getDecision().getDecisionId(),
                o.getCreatedAt(),
                voteCount);
    }
}
