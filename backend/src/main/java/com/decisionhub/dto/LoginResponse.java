
package com.decisionhub.dto;

import com.decisionhub.entity.Role;

public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private Long userId;
    private String fullName;
    private String email;
    private Role role;

    public LoginResponse(
            String accessToken,
            Long userId,
            String fullName,
            String email,
            Role role) {

        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = 3600;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
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
}
