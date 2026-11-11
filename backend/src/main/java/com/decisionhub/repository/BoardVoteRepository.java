package com.decisionhub.repository;

import com.decisionhub.entity.BoardVote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardVoteRepository extends JpaRepository<BoardVote, Long> {
    Optional<BoardVote> findByBoard_IdAndUser_Id(Long boardId, Long userId);
    boolean existsByBoard_IdAndUser_IdAndOption_Id(Long boardId, Long userId, Long optionId);
    List<BoardVote> findAllByBoard_IdAndUser_Id(Long boardId, Long userId);
    void deleteAllByBoard_Id(Long boardId);
    long countByBoard_Id(Long boardId);
    long countByUser_Id(Long userId);

    @Query("select v.option.id, count(v.id) from BoardVote v where v.board.id = :boardId group by v.option.id")
    List<Object[]> countVotesByOption(@Param("boardId") Long boardId);
}
