package com.decisionhub.dto;

import com.decisionhub.entity.BoardVisibility;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class DecisionDtos {
    private DecisionDtos() {}

    public record OptionRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 5000) String description,
            @Size(max = 10000) String pros,
            @Size(max = 10000) String cons,
            @DecimalMin("0.00") BigDecimal estimatedCost,
            @Size(max = 10000) String benefits,
            @Size(max = 10000) String risks) {}

    public record CreateBoardRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 10000) String description,
            @Size(max = 100) String category,
            @NotNull BoardVisibility visibility,
            @Size(max = 30) List<@Valid OptionRequest> options) {}

    public record UpdateBoardRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 10000) String description,
            @Size(max = 100) String category,
            @NotNull BoardVisibility visibility,
            @NotBlank @Size(max = 20) String status) {}

    public record OptionResponse(Long id, String title, String description, String pros,
                                 String cons, BigDecimal estimatedCost, String benefits,
                                 String risks) {
        public static OptionResponse from(DecisionOption option) {
            return new OptionResponse(option.getId(), option.getTitle(), option.getDescription(),
                    option.getPros(), option.getCons(), option.getEstimatedCost(),
                    option.getBenefits(), option.getRisks());
        }
    }

    public record BoardResponse(Long id, String title, String description, String category,
                                BoardVisibility visibility, String status, Long ownerId,
                                List<OptionResponse> options, LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
        public static BoardResponse from(DecisionBoard board) {
            return new BoardResponse(board.getId(), board.getTitle(), board.getDescription(),
                    board.getCategory(), board.getVisibility(), board.getStatus(),
                    board.getCreatedBy().getId(),
                    board.getOptions().stream().map(OptionResponse::from).toList(),
                    board.getCreatedAt(), board.getUpdatedAt());
        }
    }

    public record FactorRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @DecimalMin(value = "0.0001") @DecimalMax(value = "1000000") BigDecimal weight) {}

    public record FactorResponse(Long id, String name, BigDecimal weight) {}

    public record ScoreInput(
            @NotNull Long factorId,
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal score) {}

    public record ScoreRequest(@NotNull @Size(min = 1, max = 50)
                               List<@Valid ScoreInput> scores) {}

    public record RankedOption(Long optionId, String title, BigDecimal weightedScore,
                               boolean complete) {}

    public record ComparisonResponse(Long decisionId, List<FactorResponse> factors,
                                    List<RankedOption> ranking) {}
}
