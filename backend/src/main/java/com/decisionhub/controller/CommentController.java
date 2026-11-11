package com.decisionhub.controller;

import com.decisionhub.dto.ApiDtos.CommentRequest;
import com.decisionhub.dto.ApiDtos.CommentResponse;
import com.decisionhub.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/{boardId}/comments")
public class CommentController {
    private final CommentService comments;

    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @GetMapping
    public List<CommentResponse> list(@PathVariable Long boardId,
                                      @AuthenticationPrincipal UserDetails user) {
        return comments.list(boardId, user == null ? null : user.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse add(@PathVariable Long boardId, @AuthenticationPrincipal UserDetails user,
                               @Valid @RequestBody CommentRequest request) {
        return comments.add(boardId, user == null ? null : user.getUsername(), request);
    }
}
