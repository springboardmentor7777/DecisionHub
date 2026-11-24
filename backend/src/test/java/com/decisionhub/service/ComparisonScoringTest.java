package com.decisionhub.service;

import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.OptionFactorScore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComparisonScoringTest {
    @Test
    void calculatesWeightedAverageAndMarksCompleteScores() {
        ComparisonFactor cost = factor(1L, "Cost", "3");
        ComparisonFactor quality = factor(2L, "Quality", "1");

        ComparisonScoring.Result result = ComparisonScoring.calculate(
                List.of(cost, quality),
                List.of(score(cost, "80"), score(quality, "60")));

        assertEquals(new BigDecimal("75.00"), result.value());
        assertTrue(result.complete());
    }

    @Test
    void computesPartialScoreUsingOnlyRatedFactors() {
        ComparisonFactor cost = factor(1L, "Cost", "2");
        ComparisonFactor quality = factor(2L, "Quality", "1");

        ComparisonScoring.Result result = ComparisonScoring.calculate(
                List.of(cost, quality), List.of(score(cost, "70")));

        assertEquals(new BigDecimal("70.00"), result.value());
        assertFalse(result.complete());
    }

    @Test
    void givesZeroForAnOptionWithNoScores() {
        ComparisonScoring.Result result = ComparisonScoring.calculate(
                List.of(factor(1L, "Cost", "1")), List.of());

        assertEquals(BigDecimal.ZERO, result.value());
        assertFalse(result.complete());
    }

    private static ComparisonFactor factor(Long id, String name, String weight) {
        ComparisonFactor factor = new ComparisonFactor();
        factor.setId(id);
        factor.setName(name);
        factor.setWeight(new BigDecimal(weight));
        return factor;
    }

    private static OptionFactorScore score(ComparisonFactor factor, String value) {
        OptionFactorScore score = new OptionFactorScore();
        score.setFactor(factor);
        score.setScore(new BigDecimal(value));
        return score;
    }
}
