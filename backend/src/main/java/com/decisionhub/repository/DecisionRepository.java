package com.decisionhub.repository;

import com.decisionhub.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
    List<Decision> findByCreatedBy_UserId(Long userId);
}
