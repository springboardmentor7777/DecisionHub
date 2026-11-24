package com.decisionhub.repository;

import com.decisionhub.entity.ComparisonFactor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComparisonFactorRepository extends JpaRepository<ComparisonFactor, Long> {
    List<ComparisonFactor> findAllByBoardIdOrderById(Long boardId);
    Optional<ComparisonFactor> findByIdAndBoardId(Long id, Long boardId);
}
