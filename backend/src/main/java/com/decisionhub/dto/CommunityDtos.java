package com.decisionhub.dto;

import com.decisionhub.entity.BoardVisibility;
import com.decisionhub.entity.Community;
import com.decisionhub.entity.CommunityMembership;
import com.decisionhub.entity.CommunityRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class CommunityDtos {
    private CommunityDtos() {}

    public record CommunityRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 10000) String description,
            @Size(max = 100) String category,
            @NotNull BoardVisibility visibility) {}

    public record ChangeMembershipRoleRequest(@NotNull CommunityRole role) {}

    public record MemberResponse(Long userId, String name, CommunityRole role,
                                 LocalDateTime joinedAt) {
        public static MemberResponse from(CommunityMembership membership) {
            return new MemberResponse(membership.getUser().getId(), membership.getUser().getName(),
                    membership.getRole(), membership.getJoinedAt());
        }
    }

    public record CommunityResponse(Long id, String name, String description, String category,
                                    BoardVisibility visibility, Long ownerId, int memberCount,
                                    List<MemberResponse> members, LocalDateTime createdAt) {
        public static CommunityResponse from(Community community, boolean includeMembers) {
            List<MemberResponse> members = includeMembers
                    ? community.getMemberships().stream().map(MemberResponse::from).toList()
                    : List.of();
            return new CommunityResponse(community.getId(), community.getName(),
                    community.getDescription(), community.getCategory(), community.getVisibility(),
                    community.getCreatedBy().getId(), community.getMemberships().size(), members,
                    community.getCreatedAt());
        }
    }
}
