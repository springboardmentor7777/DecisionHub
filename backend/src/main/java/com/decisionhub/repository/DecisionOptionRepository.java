package com.decisionhub.repository;

import com.decisionhub.entity.DecisionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DecisionOptionRepository extends JpaRepository<DecisionOption, Long> {
    Optional<DecisionOption> findByIdAndBoardId(Long id, Long boardId);
}
