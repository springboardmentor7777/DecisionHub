
package com.decisionhub.service;

import com.decisionhub.entity.*;
import com.decisionhub.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final DecisionRepository decisionRepository;
    private final OptionRepository optionRepository;

    public VoteService(
            VoteRepository voteRepository,
            UserRepository userRepository,
            DecisionRepository decisionRepository,
            OptionRepository optionRepository) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.decisionRepository = decisionRepository;
        this.optionRepository = optionRepository;
    }

    public Vote castVote(Long decisionId, Long optionId, Integer rating) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new IllegalArgumentException("Decision not found"));

        // D3 — Poll-type-aware duplicate-vote guard.
        //
        // SINGLE_CHOICE / RATING: one vote per user per decision (original B6 rule).
        // MULTIPLE_CHOICE: multiple different options allowed per user per decision;
        //   only the same option twice is blocked (also enforced by DB unique index).
        //
        // The database (V1) enforces UNIQUE(decision_id, user_id, option_id), so
        // the same option cannot be inserted twice regardless of this service check.
        // This service guard provides a clear 409 message before hitting the DB.
        String pollType = decision.getPollType();
        if ("MULTIPLE_CHOICE".equals(pollType)) {
            // Block re-selecting the same option; allow any other option.
            if (optionId != null && voteRepository.existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
                    user.getUserId(), decision.getDecisionId(), optionId)) {
                throw new IllegalStateException("You have already selected this option");
            }
        } else {
            // SINGLE_CHOICE, RATING, or any unrecognised type: one vote per user.
            if (voteRepository.existsByUser_UserIdAndDecision_DecisionId(
                    user.getUserId(), decision.getDecisionId())) {
                throw new IllegalStateException("User has already voted on this decision");
            }
        }

        Vote vote = new Vote();
        vote.setUser(user);
        vote.setDecision(decision);
        vote.setVotedAt(LocalDateTime.now());

        if (optionId != null) {
            Option option = optionRepository.findById(optionId)
                    .orElseThrow(() -> new IllegalArgumentException("Option not found"));
            vote.setOption(option);
        }

        vote.setRating(rating);

        return voteRepository.save(vote);
    }
}
