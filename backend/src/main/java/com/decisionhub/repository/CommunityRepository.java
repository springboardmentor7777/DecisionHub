package com.decisionhub.repository;

import com.decisionhub.entity.Community;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Community> findAllByOrderByCreatedAtDesc();
    Optional<Community> findByIdAndOwner_Id(Long id, Long ownerId);
}
