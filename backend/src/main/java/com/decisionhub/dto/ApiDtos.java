package com.decisionhub.dto;

import com.decisionhub.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ApiDtos {
    private ApiDtos() {}

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 80) String displayName) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, Long id, String email, String displayName, Role role) {}
    public record ProfileUpdateRequest(@NotBlank @Size(max = 80) String displayName,
                                       @Size(max = 1000) String bio,
                                       @Size(max = 500) String interests) {}
    public record PasswordChangeRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min = 8, max = 72) String newPassword) {}
    public record ProfileResponse(Long id, String email, String displayName, Role role,
                                  String bio, String interests, Instant createdAt) {}

    public record OptionRequest(@NotBlank @Size(max = 160) String name,
                                @Size(max = 2000) String description,
                                @Size(max = 3000) String pros,
                                @Size(max = 3000) String cons,
                                @Min(0) @Max(100) Integer score) {}

    public record CreateBoardRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 3000) String description,
            @NotBlank @Size(max = 80) String category,
            @NotNull BoardVisibility visibility,
            @NotNull PollType pollType,
            @NotEmpty @Size(min = 2, max = 20) List<@Valid OptionRequest> options) {}

    public record UpdateBoardRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 3000) String description,
            @NotBlank @Size(max = 80) String category,
            @NotNull BoardVisibility visibility,
            @NotNull PollType pollType) {}

    public record OptionResponse(Long id, String name, String description, String pros, String cons, Integer score) {}
    public record BoardResponse(Long id, String title, String description, String category,
                                BoardVisibility visibility, PollType pollType, Long ownerId,
                                String ownerName, Instant createdAt, List<OptionResponse> options) {}

    public record VoteRequest(@NotNull Long optionId, @Min(1) @Max(5) Integer rating, boolean anonymous) {}
    public record VoteResponse(Long boardId, long totalVotes, Map<Long, Long> votesByOption) {}
    public record CommentRequest(@NotBlank @Size(max = 3000) String content, Long parentId) {}
    public record CommentResponse(Long id, Long boardId, Long parentId, Long authorId,
                                  String authorName, String content, Instant createdAt,
                                  List<CommentResponse> replies) {}

    public record CreateCommunityRequest(@NotBlank @Size(max = 100) String name,
                                         @Size(max = 2000) String description,
                                         @NotNull CommunityCategory category) {}
    public record CommunityResponse(Long id, String name, String description, CommunityCategory category,
                                    Long ownerId, String ownerName, long memberCount, Instant createdAt) {}
    public record CommunityMemberResponse(Long userId, String displayName, Role role, Instant joinedAt) {}
    public record NotificationResponse(Long id, String message, String link, boolean read, Instant createdAt) {}

    public record DashboardResponse(long activeDecisions, long votesCast, long communityMemberships,
                                    Map<String, Long> popularCategories) {}
}
