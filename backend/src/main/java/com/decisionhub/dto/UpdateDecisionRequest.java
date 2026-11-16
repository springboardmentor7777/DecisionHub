package com.decisionhub.dto;

import jakarta.validation.constraints.Size;

public class UpdateDecisionRequest {

    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    private String description;
    private String status;
    private String visibility;
    private String endsAt; // ISO-8601 string; parsed in controller

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getEndsAt() { return endsAt; }
    public void setEndsAt(String endsAt) { this.endsAt = endsAt; }
}
