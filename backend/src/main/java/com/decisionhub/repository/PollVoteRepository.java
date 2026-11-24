package com.decisionhub.repository;

import com.decisionhub.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    boolean existsByPollIdAndVoterId(Long pollId, Long voterId);
    List<PollVote> findAllByPollId(Long pollId);
}
