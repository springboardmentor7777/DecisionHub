package com.decisionhub.dto;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long commentId;
    private Long decisionId;
    private Long userId;
    private String userFullName;
    private Long parentCommentId;
    private String commentText;
    private LocalDateTime createdAt;

    public CommentResponse() {}

    public CommentResponse(Long commentId, Long decisionId, Long userId,
                           String userFullName, Long parentCommentId,
                           String commentText, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.decisionId = decisionId;
        this.userId = userId;
        this.userFullName = userFullName;
        this.parentCommentId = parentCommentId;
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public Long getDecisionId() { return decisionId; }
    public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
