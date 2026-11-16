package com.decisionhub.service;

import com.decisionhub.dto.OptionResponse;
import com.decisionhub.entity.Decision;
import com.decisionhub.entity.Option;
import com.decisionhub.repository.DecisionRepository;
import com.decisionhub.repository.OptionRepository;
import com.decisionhub.repository.VoteRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OptionService {

    private final OptionRepository optionRepository;
    private final DecisionRepository decisionRepository;
    private final VoteRepository voteRepository;

    public OptionService(OptionRepository optionRepository,
                         DecisionRepository decisionRepository,
                         VoteRepository voteRepository) {
        this.optionRepository = optionRepository;
        this.decisionRepository = decisionRepository;
        this.voteRepository = voteRepository;
    }

    public Option addOption(Long decisionId, String optionText) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new IllegalArgumentException("Decision not found"));

        Option option = new Option();
        option.setDecision(decision);
        option.setOptionText(optionText);
        option.setCreatedAt(LocalDateTime.now());

        return optionRepository.save(option);
    }

    /**
     * Returns all options for a decision as raw entities.
     * Preserved for callers that need the entity (e.g. POST single-option path).
     */
    public List<Option> getOptionsForDecision(Long decisionId) {
        if (!decisionRepository.existsById(decisionId)) {
            throw new IllegalArgumentException("Decision not found");
        }
        return optionRepository.findByDecision_DecisionIdOrderByCreatedAtAsc(decisionId);
    }

    /**
     * Returns all options for a decision pre-populated with accurate vote counts.
     *
     * Uses a single GROUP BY query to fetch all vote counts in one round-trip,
     * then maps them into OptionResponse objects — eliminating the N+1 problem
     * that arises when countByOption_OptionId() is called once per option.
     *
     * Options with zero votes are included with voteCount = 0.
     */
    public List<OptionResponse> getOptionsWithVoteCounts(Long decisionId) {
        List<Option> options = getOptionsForDecision(decisionId);

        if (options.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect all option IDs for the bulk query
        List<Long> optionIds = options.stream()
                .map(Option::getOptionId)
                .collect(Collectors.toList());

        // Single GROUP BY query: returns [optionId, count] rows only for options
        // that have at least one vote. Options with no votes are absent.
        Map<Long, Long> voteCountByOptionId = voteRepository
                .countVotesGroupedByOptionIds(optionIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long)  row[0],   // optionId
                        row -> (Long)  row[1]    // vote count
                ));

        // Build responses; default to 0 for options absent from the map
        return options.stream()
                .map(o -> new OptionResponse(
                        o.getOptionId(),
                        o.getOptionText(),
                        o.getDecision().getDecisionId(),
                        o.getCreatedAt(),
                        voteCountByOptionId.getOrDefault(o.getOptionId(), 0L)))
                .collect(Collectors.toList());
    }

    /**
     * Returns the vote count for a single option.
     * Used by the POST /options path (one newly created option).
     */
    public long getVoteCount(Long optionId) {
        return voteRepository.countByOption_OptionId(optionId);
    }
}
