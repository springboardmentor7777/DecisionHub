
package com.decisionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommunityRequest {

    @NotBlank(message = "Community name is required")
    @Size(max = 150, message = "Community name cannot exceed 150 characters")
    private String communityName;

    private String description;

    public CommunityRequest() {
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
}
