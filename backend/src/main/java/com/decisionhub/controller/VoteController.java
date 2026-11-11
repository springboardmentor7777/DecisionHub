package com.decisionhub.controller;

import com.decisionhub.dto.ApiDtos.VoteRequest;
import com.decisionhub.dto.ApiDtos.VoteResponse;
import com.decisionhub.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/{boardId}/votes")
public class VoteController {
    private final VoteService votes;

    public VoteController(VoteService votes) {
        this.votes = votes;
    }

    @PostMapping
    public VoteResponse vote(@PathVariable Long boardId, @AuthenticationPrincipal UserDetails user,
                             @Valid @RequestBody VoteRequest request) {
        return votes.vote(boardId, user == null ? null : user.getUsername(), request);
    }

    @GetMapping
    public VoteResponse results(@PathVariable Long boardId,
                                @AuthenticationPrincipal UserDetails user) {
        return votes.results(boardId, user == null ? null : user.getUsername());
    }
}
