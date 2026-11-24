package com.decisionhub.repository;

import com.decisionhub.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    Optional<PollOption> findByIdAndPollId(Long id, Long pollId);
}
