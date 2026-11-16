package com.decisionhub.controller;

import com.decisionhub.dto.CommentRequest;
import com.decisionhub.dto.CommentResponse;
import com.decisionhub.entity.Comment;
import com.decisionhub.service.CommentService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/decisions/{decisionId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // POST /api/decisions/{decisionId}/comments
    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long decisionId,
            @Valid @RequestBody CommentRequest request) {

        Comment comment = commentService.addComment(
                decisionId,
                request.getParentId(),
                request.getCommentText());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(comment));
    }

    // GET /api/decisions/{decisionId}/comments
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long decisionId) {
        List<CommentResponse> list = commentService.getComments(decisionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    private CommentResponse toResponse(Comment c) {
        String fullName = (c.getUser() != null) ? c.getUser().getFullName() : null;
        return new CommentResponse(
                c.getCommentId(),
                c.getDecision().getDecisionId(),
                c.getUser().getUserId(),
                fullName,
                c.getParentComment() != null ? c.getParentComment().getCommentId() : null,
                c.getCommentText(),
                c.getCreatedAt());
    }
}
