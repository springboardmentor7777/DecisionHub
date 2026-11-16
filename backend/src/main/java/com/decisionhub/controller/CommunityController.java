package com.decisionhub.controller;

import com.decisionhub.dto.CommunityRequest;
import com.decisionhub.dto.CommunityResponse;
import com.decisionhub.entity.Community;
import com.decisionhub.service.CommunityService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    // POST /api/communities
    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(
            @Valid @RequestBody CommunityRequest request) {

        Community community = communityService.createCommunity(
                request.getCommunityName(), request.getDescription());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(community));
    }

    // GET /api/communities
    @GetMapping
    public ResponseEntity<List<CommunityResponse>> getAllCommunities() {
        List<CommunityResponse> list = communityService.getAllCommunities()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // GET /api/communities/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponse> getCommunityById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(communityService.getCommunityById(id)));
    }

    private CommunityResponse toResponse(Community c) {
        return new CommunityResponse(
                c.getCommunityId(),
                c.getCommunityName(),
                c.getDescription(),
                c.getCreatedBy() != null ? c.getCreatedBy().getEmail() : null,
                c.getCreatedAt());
    }
}
