package com.decisionhub.repository;

import com.decisionhub.entity.CommunityMembership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, Long> {
    boolean existsByCommunity_IdAndUser_Id(Long communityId, Long userId);
    Optional<CommunityMembership> findByCommunity_IdAndUser_Id(Long communityId, Long userId);
    List<CommunityMembership> findAllByCommunity_Id(Long communityId);
    void deleteAllByCommunity_Id(Long communityId);
    long countByUser_Id(Long userId);
    long countByCommunity_Id(Long communityId);
}
