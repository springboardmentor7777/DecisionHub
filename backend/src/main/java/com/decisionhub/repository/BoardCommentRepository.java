package com.decisionhub.repository;

import com.decisionhub.entity.BoardComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    List<BoardComment> findAllByBoard_IdAndParentIsNullOrderByCreatedAtAsc(Long boardId);
    List<BoardComment> findAllByBoard_IdOrderByCreatedAtDesc(Long boardId);
}
