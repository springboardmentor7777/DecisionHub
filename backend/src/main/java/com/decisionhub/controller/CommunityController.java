package com.decisionhub.controller;

import com.decisionhub.dto.CommunityDtos.*;
import com.decisionhub.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityResponse create(Authentication auth, @Valid @RequestBody CommunityRequest request) {
        return communityService.create(auth.getName(), request);
    }

    @GetMapping
    public List<CommunityResponse> list(Authentication auth,
                                        @RequestParam(required = false) String category) {
        return communityService.list(auth.getName(), category);
    }

    @GetMapping("/{id}")
    public CommunityResponse get(Authentication auth, @PathVariable Long id) {
        return communityService.get(auth.getName(), id);
    }

    @PutMapping("/{id}")
    public CommunityResponse update(Authentication auth, @PathVariable Long id,
                                    @Valid @RequestBody CommunityRequest request) {
        return communityService.update(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        communityService.delete(auth.getName(), id);
    }

    @PostMapping("/{id}/join")
    public MemberResponse join(Authentication auth, @PathVariable Long id) {
        return communityService.join(auth.getName(), id);
    }

    @DeleteMapping("/{id}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(Authentication auth, @PathVariable Long id) {
        communityService.leave(auth.getName(), id);
    }

    @GetMapping("/{id}/members")
    public List<MemberResponse> members(Authentication auth, @PathVariable Long id) {
        return communityService.members(auth.getName(), id);
    }

    @PostMapping("/{id}/members/{userId}")
    public MemberResponse addMember(Authentication auth, @PathVariable Long id,
                                    @PathVariable Long userId) {
        return communityService.addMember(auth.getName(), id, userId);
    }

    @PutMapping("/{id}/members/{userId}")
    public MemberResponse changeRole(Authentication auth, @PathVariable Long id,
                                     @PathVariable Long userId,
                                     @Valid @RequestBody ChangeMembershipRoleRequest request) {
        return communityService.changeRole(auth.getName(), id, userId, request);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(Authentication auth, @PathVariable Long id, @PathVariable Long userId) {
        communityService.removeMember(auth.getName(), id, userId);
    }
}
