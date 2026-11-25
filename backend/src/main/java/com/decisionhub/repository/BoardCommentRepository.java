package com.decisionhub.repository;

import com.decisionhub.entity.BoardComment;
import com.decisionhub.entity.BoardVisibility;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    List<BoardComment> findAllByBoard_IdAndParentIsNullOrderByCreatedAtAsc(Long boardId);
    List<BoardComment> findAllByBoard_IdOrderByCreatedAtDesc(Long boardId);
    List<BoardComment> findTop50ByBoard_VisibilityOrderByCreatedAtDesc(BoardVisibility visibility);
    Optional<BoardComment> findByIdAndBoard_Visibility(Long id, BoardVisibility visibility);
}
