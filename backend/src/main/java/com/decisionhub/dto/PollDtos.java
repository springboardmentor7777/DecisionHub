package com.decisionhub.dto;

import com.decisionhub.entity.Poll;
import com.decisionhub.entity.PollOption;
import com.decisionhub.entity.PollType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PollDtos {
    private PollDtos() {}

    public record CreatePollRequest(
            @NotBlank @Size(max = 250) String question,
            @NotNull PollType type,
            boolean anonymous,
            @Size(max = 50) List<@NotBlank @Size(max = 200) String> options) {}

    public record CastVoteRequest(
            @Size(max = 50) List<@NotNull Long> optionIds,
            @Min(1) @Max(5) Integer rating) {}

    public record PollOptionResponse(Long id, String label) {
        public static PollOptionResponse from(PollOption option) {
            return new PollOptionResponse(option.getId(), option.getLabel());
        }
    }

    public record PollResponse(Long id, Long decisionId, String question, PollType type,
                               boolean anonymous, boolean active, List<PollOptionResponse> options,
                               LocalDateTime createdAt) {
        public static PollResponse from(Poll poll) {
            return new PollResponse(poll.getId(), poll.getBoard().getId(), poll.getQuestion(),
                    poll.getType(), poll.isAnonymous(), poll.isActive(),
                    poll.getOptions().stream().map(PollOptionResponse::from).toList(),
                    poll.getCreatedAt());
        }
    }

    public record PollOptionResult(Long optionId, String label, long votes,
                                   BigDecimal percentage) {}

    public record PollResults(Long pollId, long totalVotes, BigDecimal averageRating,
                              List<PollOptionResult> options) {}
}
