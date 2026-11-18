package com.decisionhub.dto;

import java.time.LocalDateTime;

public class DecisionResponse {

    private Long decisionId;
    private String title;
    private String description;
    private Long communityId;
    private String pollType;
    private String visibility;
    private String status;
    private LocalDateTime endsAt;
    private LocalDateTime createdAt;
    private String createdByEmail;

    public DecisionResponse() {}

    public DecisionResponse(Long decisionId, String title, String description,
                            Long communityId, String pollType, String visibility,
                            String status, LocalDateTime endsAt,
                            LocalDateTime createdAt, String createdByEmail) {
        this.decisionId = decisionId;
        this.title = title;
        this.description = description;
        this.communityId = communityId;
        this.pollType = pollType;
        this.visibility = visibility;
        this.status = status;
        this.endsAt = endsAt;
        this.createdAt = createdAt;
        this.createdByEmail = createdByEmail;
    }

    public Long getDecisionId() { return decisionId; }
    public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }

    public String getPollType() { return pollType; }
    public void setPollType(String pollType) { this.pollType = pollType; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String createdByEmail) { this.createdByEmail = createdByEmail; }
}
