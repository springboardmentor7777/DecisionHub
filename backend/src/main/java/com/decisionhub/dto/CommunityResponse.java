
package com.decisionhub.dto;

import java.time.LocalDateTime;

public class CommunityResponse {

    private Long communityId;
    private String communityName;
    private String description;
    private String createdByEmail;
    private LocalDateTime createdAt;

    public CommunityResponse() {
    }

    public CommunityResponse(Long communityId, String communityName,
                             String description, String createdByEmail,
                             LocalDateTime createdAt) {
        this.communityId = communityId;
        this.communityName = communityName;
        this.description = description;
        this.createdByEmail = createdByEmail;
        this.createdAt = createdAt;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
