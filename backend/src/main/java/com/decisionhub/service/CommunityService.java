package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.*;
import com.decisionhub.entity.*;
import com.decisionhub.repository.CommunityMembershipRepository;
import com.decisionhub.repository.CommunityRepository;
import com.decisionhub.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityService {
    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final UserRepository users;

    public CommunityService(CommunityRepository communities, CommunityMembershipRepository memberships,
                            UserRepository users) {
        this.communities = communities;
        this.memberships = memberships;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> list() {
        return communities.findAllByOrderByCreatedAtDesc().stream().map(this::response).toList();
    }

    @Transactional
    public CommunityResponse create(String email, CreateCommunityRequest request) {
        AppUser owner = user(email);
        if (communities.existsByNameIgnoreCase(request.name().trim())) {
            throw new IllegalArgumentException("A community with this name already exists");
        }
        Community community = communities.save(new Community(request.name().trim(), request.description(),
                request.category(), owner));
        memberships.save(new CommunityMembership(community, owner));
        return response(community);
    }

    @Transactional
    public CommunityResponse join(Long id, String email) {
        Community community = findCommunity(id);
        AppUser user = user(email);
        if (!memberships.existsByCommunity_IdAndUser_Id(id, user.getId())) {
            memberships.save(new CommunityMembership(community, user));
        }
        return response(community);
    }

    @Transactional
    public void leave(Long id, String email) {
        Community community = findCommunity(id);
        AppUser user = user(email);
        if (community.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Community owners cannot leave; delete the community instead");
        }
        memberships.findByCommunity_IdAndUser_Id(id, user.getId())
                .ifPresent(memberships::delete);
    }

    @Transactional(readOnly = true)
    public List<CommunityMemberResponse> members(Long id) {
        Community community = findCommunity(id);
        return memberships.findAllByCommunity_Id(community.getId()).stream()
                .map(member -> new CommunityMemberResponse(member.getUser().getId(),
                        member.getUser().getDisplayName(), member.getUser().getRole(), member.getJoinedAt()))
                .toList();
    }

    @Transactional
    public void delete(Long id, String email) {
        Community community = findCommunity(id);
        AppUser caller = user(email);
        if (!community.getOwner().getId().equals(caller.getId()) && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only the community owner or an administrator can delete this community");
        }
        memberships.deleteAllByCommunity_Id(id);
        communities.delete(community);
    }

    private Community findCommunity(Long id) {
        return communities.findById(id).orElseThrow(() -> new ResourceNotFoundException("Community not found"));
    }

    private AppUser user(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CommunityResponse response(Community community) {
        return new CommunityResponse(community.getId(), community.getName(), community.getDescription(),
                community.getCategory(), community.getOwner().getId(), community.getOwner().getDisplayName(),
                memberships.countByCommunity_Id(community.getId()), community.getCreatedAt());
    }
}
