package com.decisionhub.repository;

import com.decisionhub.entity.BoardVisibility;
import com.decisionhub.entity.DecisionBoard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DecisionBoardRepository extends JpaRepository<DecisionBoard, Long> {
    @EntityGraph(attributePaths = {"owner", "options"})
    List<DecisionBoard> findAllByVisibilityOrderByCreatedAtDesc(BoardVisibility visibility);

    @EntityGraph(attributePaths = {"owner", "options"})
    List<DecisionBoard> findAllByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"owner", "options"})
    Optional<DecisionBoard> findWithOptionsById(Long id);

    long countByOwner_Id(Long ownerId);
    long countByCategoryIgnoreCase(String category);

    @Query("select b.category, count(b.id) from DecisionBoard b group by b.category order by count(b.id) desc")
    List<Object[]> countByCategory();
}
