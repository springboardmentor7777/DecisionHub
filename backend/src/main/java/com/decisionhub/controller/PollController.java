package com.decisionhub.controller;

import com.decisionhub.dto.PollDtos.*;
import com.decisionhub.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PollController {
    private final PollService pollService;

    @PostMapping("/api/decisions/{decisionId}/polls")
    @ResponseStatus(HttpStatus.CREATED)
    public PollResponse create(Authentication auth, @PathVariable Long decisionId,
                               @Valid @RequestBody CreatePollRequest request) {
        return pollService.create(auth.getName(), decisionId, request);
    }

    @GetMapping("/api/decisions/{decisionId}/polls")
    public List<PollResponse> list(Authentication auth, @PathVariable Long decisionId) {
        return pollService.listByBoard(auth.getName(), decisionId);
    }

    @GetMapping("/api/polls/{pollId}")
    public PollResponse get(Authentication auth, @PathVariable Long pollId) {
        return pollService.get(auth.getName(), pollId);
    }

    @PostMapping("/api/polls/{pollId}/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public void vote(Authentication auth, @PathVariable Long pollId,
                     @Valid @RequestBody CastVoteRequest request) {
        pollService.castVote(auth.getName(), pollId, request);
    }

    @GetMapping("/api/polls/{pollId}/results")
    public PollResults results(Authentication auth, @PathVariable Long pollId) {
        return pollService.results(auth.getName(), pollId);
    }

    @PatchMapping("/api/polls/{pollId}/close")
    public PollResponse close(Authentication auth, @PathVariable Long pollId) {
        return pollService.close(auth.getName(), pollId);
    }
}
