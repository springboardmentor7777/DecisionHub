package com.decisionhub.controller;

import com.decisionhub.dto.VoteRequest;
import com.decisionhub.dto.VoteResponse;
import com.decisionhub.entity.Vote;
import com.decisionhub.service.VoteService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/decisions/{decisionId}/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    // POST /api/decisions/{decisionId}/votes
    @PostMapping
    public ResponseEntity<VoteResponse> castVote(
            @PathVariable Long decisionId,
            @RequestBody VoteRequest request) {

        Vote vote = voteService.castVote(
                decisionId,
                request.getOptionId(),
                request.getRating());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(vote));
    }

    private VoteResponse toResponse(Vote v) {
        return new VoteResponse(
                v.getVoteId(),
                v.getDecision().getDecisionId(),
                v.getOption() != null ? v.getOption().getOptionId() : null,
                v.getRating(),
                v.getVotedAt());
    }
}
