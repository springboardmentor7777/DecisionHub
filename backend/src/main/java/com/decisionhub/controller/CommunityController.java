package com.decisionhub.controller;

import com.decisionhub.dto.ApiDtos.*;
import com.decisionhub.service.CommunityService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {
    private final CommunityService communities;

    public CommunityController(CommunityService communities) {
        this.communities = communities;
    }

    @GetMapping
    public List<CommunityResponse> list() {
        return communities.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityResponse create(@AuthenticationPrincipal UserDetails user,
                                    @Valid @RequestBody CreateCommunityRequest request) {
        return communities.create(user.getUsername(), request);
    }

    @PostMapping("/{id}/members")
    public CommunityResponse join(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return communities.join(id, user.getUsername());
    }

    @DeleteMapping("/{id}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        communities.leave(id, user.getUsername());
    }

    @GetMapping("/{id}/members")
    public List<CommunityMemberResponse> members(@PathVariable Long id) {
        return communities.members(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        communities.delete(id, user.getUsername());
    }
}
