package com.decisionhub.service;

import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.OptionFactorScore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ComparisonScoring {
    private ComparisonScoring() {}

    static Result calculate(List<ComparisonFactor> factors, List<OptionFactorScore> scores) {
        BigDecimal weightedTotal = BigDecimal.ZERO;
        BigDecimal usedWeight = BigDecimal.ZERO;
        Set<Long> scoredFactorIds = new HashSet<>();
        for (OptionFactorScore score : scores) {
            BigDecimal weight = score.getFactor().getWeight();
            weightedTotal = weightedTotal.add(weight.multiply(score.getScore()));
            usedWeight = usedWeight.add(weight);
            scoredFactorIds.add(score.getFactor().getId());
        }
        BigDecimal value = usedWeight.signum() == 0 ? BigDecimal.ZERO
                : weightedTotal.divide(usedWeight, 2, RoundingMode.HALF_UP);
        boolean complete = !factors.isEmpty() && scoredFactorIds.size() == factors.size();
        return new Result(value, complete);
    }

    record Result(BigDecimal value, boolean complete) {}
}
