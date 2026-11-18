
package com.decisionhub.repository;

import com.decisionhub.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    /**
     * B6 — Duplicate-vote guard.
     * Returns true if the given user has already cast any vote on the given decision.
     * Used by VoteService.castVote() for SINGLE_CHOICE and RATING polls.
     */
    boolean existsByUser_UserIdAndDecision_DecisionId(Long userId, Long decisionId);

    /**
     * D3 — MULTIPLE_CHOICE duplicate-option guard.
     * Returns true if the given user has already voted for this specific option
     * on this decision.  Used to prevent double-selecting the same option while
     * still allowing multiple different options in a MULTIPLE_CHOICE poll.
     */
    boolean existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
            Long userId, Long decisionId, Long optionId);

    /**
     * Returns the number of votes cast for a specific option.
     * Used for the single-option (POST) path where only one option is involved.
     */
    long countByOption_OptionId(Long optionId);

    /**
     * Bulk vote-count query: returns one Object[]{optionId, count} row per option
     * that has at least one vote, restricted to the given option-ID set.
     * Options with zero votes are absent from the result — callers default them to 0.
     *
     * Replaces N individual countByOption_OptionId() calls with a single
     * GROUP BY query, eliminating the N+1 problem on GET /options.
     */
    @Query("SELECT v.option.optionId, COUNT(v) FROM Vote v " +
           "WHERE v.option.optionId IN :optionIds " +
           "GROUP BY v.option.optionId")
    List<Object[]> countVotesGroupedByOptionIds(@Param("optionIds") Collection<Long> optionIds);
}
