package com.decisionhub.repository;

import com.decisionhub.entity.DecisionOption;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionOptionRepository extends JpaRepository<DecisionOption, Long> {
    Optional<DecisionOption> findByIdAndBoard_Id(Long id, Long boardId);
}
