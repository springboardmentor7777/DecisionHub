package com.decisionhub.dto;

public class VoteRequest {

    private Long optionId;   // null for RATING polls
    private Integer rating;  // null for option-based polls; 1-5 for RATING

    public Long getOptionId() { return optionId; }
    public void setOptionId(Long optionId) { this.optionId = optionId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
