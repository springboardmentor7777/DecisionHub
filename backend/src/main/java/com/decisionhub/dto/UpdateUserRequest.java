package com.decisionhub.dto;

import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
