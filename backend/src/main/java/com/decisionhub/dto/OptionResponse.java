package com.decisionhub.dto;

import java.time.LocalDateTime;

public class OptionResponse {

    private Long optionId;
    private String optionText;
    private Long decisionId;
    private LocalDateTime createdAt;
    private long voteCount;

    public OptionResponse() {}

    public OptionResponse(Long optionId, String optionText, Long decisionId,
                          LocalDateTime createdAt, long voteCount) {
        this.optionId = optionId;
        this.optionText = optionText;
        this.decisionId = decisionId;
        this.createdAt = createdAt;
        this.voteCount = voteCount;
    }

    public Long getOptionId() { return optionId; }
    public void setOptionId(Long optionId) { this.optionId = optionId; }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public Long getDecisionId() { return decisionId; }
    public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getVoteCount() { return voteCount; }
    public void setVoteCount(long voteCount) { this.voteCount = voteCount; }
}
