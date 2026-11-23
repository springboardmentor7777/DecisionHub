package com.decisionhub.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PollResponse {

    private Long decisionId;
    private String title;
    private String description;
    private String pollType;
    private String status;
    private String visibility;
    private LocalDateTime endsAt;
    private LocalDateTime createdAt;
    private String createdByName;
    private List<OptionResponse> options;

    public PollResponse() {}

    public Long getDecisionId() { return decisionId; }
    public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPollType() { return pollType; }
    public void setPollType(String pollType) { this.pollType = pollType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public List<OptionResponse> getOptions() { return options; }
    public void setOptions(List<OptionResponse> options) { this.options = options; }
}
