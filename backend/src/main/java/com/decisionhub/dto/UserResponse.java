package com.decisionhub.dto;

import com.decisionhub.entity.Role;
import java.time.LocalDateTime;

public class UserResponse {

    private Long userId;
    private String fullName;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long userId, String fullName, String email,
                        Role role, LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}