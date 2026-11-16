package com.decisionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    private Long parentId; // nullable — top-level comment if absent

    @NotBlank(message = "Comment text is required")
    @Size(max = 2000, message = "Comment must be at most 2000 characters")
    private String commentText;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
}
