package com.decisionhub.service;

import com.decisionhub.dto.CommunityDtos.*;
import com.decisionhub.entity.*;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.CommunityMembershipRepository;
import com.decisionhub.repository.CommunityRepository;
import com.decisionhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommunityResponse create(String email, CommunityRequest request) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Community community = new Community();
        community.setName(request.name().trim());
        community.setDescription(request.description());
        community.setCategory(CategoryCatalog.normalize(request.category()));
        community.setVisibility(request.visibility());
        community.setCreatedBy(owner);
        communityRepository.save(community);

        CommunityMembership membership = new CommunityMembership();
        membership.setCommunity(community);
        membership.setUser(owner);
        membership.setRole(CommunityRole.OWNER);
        membershipRepository.save(membership);
        community.getMemberships().add(membership);
        return CommunityResponse.from(community, true);
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> list(String email, String category) {
        String normalizedCategory = CategoryCatalog.normalize(category);
        Long userId = currentUserId(email);
        return communityRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(community -> community.getVisibility() == BoardVisibility.PUBLIC
                        || membershipRepository.existsByCommunityIdAndUserId(
                        community.getId(), userId))
                .filter(community -> normalizedCategory == null
                        || normalizedCategory.equalsIgnoreCase(community.getCategory()))
                .map(community -> CommunityResponse.from(community, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public CommunityResponse get(String email, Long id) {
        Community community = accessibleCommunity(email, id);
        boolean member = membership(community.getId(), currentUserId(email)) != null;
        return CommunityResponse.from(community, member);
    }

    @Transactional
    public CommunityResponse update(String email, Long id, CommunityRequest request) {
        Community community = ownedCommunity(email, id);
        community.setName(request.name().trim());
        community.setDescription(request.description());
        community.setCategory(CategoryCatalog.normalize(request.category()));
        community.setVisibility(request.visibility());
        return CommunityResponse.from(communityRepository.save(community), true);
    }

    @Transactional
    public void delete(String email, Long id) {
        communityRepository.delete(ownedCommunity(email, id));
    }

    @Transactional
    public MemberResponse join(String email, Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CommunityMembership existing = membership(community.getId(), user.getId());
        if (existing != null) {
            return MemberResponse.from(existing);
        }
        if (community.getVisibility() == BoardVisibility.PRIVATE) {
            throw new ForbiddenException("This community is private");
        }
        return MemberResponse.from(addMember(community, user, CommunityRole.MEMBER));
    }

    @Transactional
    public void leave(String email, Long id) {
        CommunityMembership membership = currentMembership(email, id);
        if (membership.getRole() == CommunityRole.OWNER) {
            throw new BadRequestException("The community owner cannot leave; transfer ownership first");
        }
        membershipRepository.delete(membership);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> members(String email, Long id) {
        accessibleCommunity(email, id);
        requireMembership(id, currentUserId(email));
        return membershipRepository.findAllByCommunityIdOrderByJoinedAtAsc(id).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse addMember(String actingEmail, Long communityId, Long userId) {
        Community community = ownedOrModeratorCommunity(actingEmail, communityId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CommunityMembership existing = membership(communityId, userId);
        return MemberResponse.from(existing == null
                ? addMember(community, user, CommunityRole.MEMBER)
                : existing);
    }

    @Transactional
    public MemberResponse changeRole(String actingEmail, Long communityId, Long userId,
                                    ChangeMembershipRoleRequest request) {
        ownedCommunity(actingEmail, communityId);
        if (request.role() == CommunityRole.OWNER) {
            throw new BadRequestException("Ownership cannot be assigned through this endpoint");
        }
        CommunityMembership target = membership(communityId, userId);
        if (target == null) {
            throw new ResourceNotFoundException("Community member not found");
        }
        if (target.getRole() == CommunityRole.OWNER) {
            throw new BadRequestException("The community owner role cannot be changed here");
        }
        target.setRole(request.role());
        return MemberResponse.from(target);
    }

    @Transactional
    public void removeMember(String actingEmail, Long communityId, Long userId) {
        ownedOrModeratorCommunity(actingEmail, communityId);
        CommunityMembership actor = currentMembership(actingEmail, communityId);
        CommunityMembership target = membership(communityId, userId);
        if (target == null) {
            throw new ResourceNotFoundException("Community member not found");
        }
        if (target.getRole() == CommunityRole.OWNER
                || (actor.getRole() == CommunityRole.MODERATOR
                && target.getRole() == CommunityRole.MODERATOR)) {
            throw new ForbiddenException("You cannot remove this community member");
        }
        membershipRepository.delete(target);
    }

    private Community accessibleCommunity(String email, Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        if (community.getVisibility() == BoardVisibility.PRIVATE
                && membership(community.getId(), currentUserId(email)) == null) {
            throw new ResourceNotFoundException("Community not found");
        }
        return community;
    }

    private Community ownedCommunity(String email, Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        if (!community.getCreatedBy().getEmail().equals(email)) {
            throw new ForbiddenException("Only the community owner can do this");
        }
        return community;
    }

    private Community ownedOrModeratorCommunity(String email, Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        CommunityMembership actor = membership(id, currentUserId(email));
        if (actor == null || (actor.getRole() != CommunityRole.OWNER
                && actor.getRole() != CommunityRole.MODERATOR)) {
            throw new ForbiddenException("Community owner or moderator role required");
        }
        return community;
    }

    private CommunityMembership currentMembership(String email, Long communityId) {
        CommunityMembership member = membership(communityId, currentUserId(email));
        if (member == null) {
            throw new ResourceNotFoundException("Community membership not found");
        }
        return member;
    }

    private void requireMembership(Long communityId, Long userId) {
        if (membership(communityId, userId) == null) {
            throw new ForbiddenException("Community membership required");
        }
    }

    private CommunityMembership membership(Long communityId, Long userId) {
        return membershipRepository.findByCommunityIdAndUserId(communityId, userId).orElse(null);
    }

    private Long currentUserId(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    private CommunityMembership addMember(Community community, User user, CommunityRole role) {
        CommunityMembership membership = new CommunityMembership();
        membership.setCommunity(community);
        membership.setUser(user);
        membership.setRole(role);
        return membershipRepository.save(membership);
    }
}
