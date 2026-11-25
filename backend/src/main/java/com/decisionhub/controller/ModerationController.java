package com.decisionhub.controller;

import com.decisionhub.dto.ApiDtos.ModerationCommentResponse;
import com.decisionhub.service.ModerationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/moderation")
public class ModerationController {
    private final ModerationService moderation;

    public ModerationController(ModerationService moderation) {
        this.moderation = moderation;
    }

    @GetMapping("/comments")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public List<ModerationCommentResponse> recentComments() {
        return moderation.recentComments();
    }

    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public void deleteComment(@PathVariable Long id, @AuthenticationPrincipal UserDetails moderator) {
        moderation.deleteComment(id, moderator.getUsername());
    }

    @DeleteMapping("/boards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public void deleteBoard(@PathVariable Long id, @AuthenticationPrincipal UserDetails moderator) {
        moderation.deleteBoard(id, moderator.getUsername());
    }
}
