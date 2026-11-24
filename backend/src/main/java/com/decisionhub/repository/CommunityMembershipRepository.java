package com.decisionhub.repository;

import com.decisionhub.entity.CommunityMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, Long> {
    Optional<CommunityMembership> findByCommunityIdAndUserId(Long communityId, Long userId);
    boolean existsByCommunityIdAndUserId(Long communityId, Long userId);
    List<CommunityMembership> findAllByCommunityIdOrderByJoinedAtAsc(Long communityId);
}
