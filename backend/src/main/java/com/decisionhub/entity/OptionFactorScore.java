package com.decisionhub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "option_factor_scores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_option_factor", columnNames = {"option_id", "factor_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class OptionFactorScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private DecisionOption option;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factor_id", nullable = false)
    private ComparisonFactor factor;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;
}
