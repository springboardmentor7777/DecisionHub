package com.decisionhub.repository;

import com.decisionhub.entity.DecisionBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DecisionBoardRepository extends JpaRepository<DecisionBoard, Long> {
    List<DecisionBoard> findAllByOrderByCreatedAtDesc();
    Optional<DecisionBoard> findByIdAndCreatedByEmail(Long id, String email);
}
