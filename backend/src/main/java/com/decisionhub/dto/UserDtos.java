package com.decisionhub.dto;

import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UserDtos {
    private UserDtos() {}

    public record UserResponse(Long id, String name, String email, String role,
                               String bio, String interests, boolean enabled,
                               LocalDateTime createdAt) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name(),
                    u.getBio(), u.getInterests(), u.isEnabled(), u.getCreatedAt());
        }
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String bio,
            @Size(max = 255) String interests) {}

    public record ChangeRoleRequest(@NotNull Role role) {}
}