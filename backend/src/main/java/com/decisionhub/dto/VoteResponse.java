package com.decisionhub.dto;

import java.time.LocalDateTime;

public class VoteResponse {

    private Long voteId;
    private Long decisionId;
    private Long optionId;
    private Integer rating;
    private LocalDateTime votedAt;

    public VoteResponse() {}

    public VoteResponse(Long voteId, Long decisionId, Long optionId,
                        Integer rating, LocalDateTime votedAt) {
        this.voteId = voteId;
        this.decisionId = decisionId;
        this.optionId = optionId;
        this.rating = rating;
        this.votedAt = votedAt;
    }

    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }

    public Long getDecisionId() { return decisionId; }
    public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }

    public Long getOptionId() { return optionId; }
    public void setOptionId(Long optionId) { this.optionId = optionId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public LocalDateTime getVotedAt() { return votedAt; }
    public void setVotedAt(LocalDateTime votedAt) { this.votedAt = votedAt; }
}
