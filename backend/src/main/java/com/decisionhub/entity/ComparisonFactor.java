package com.decisionhub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decision_factors", uniqueConstraints = {
        @UniqueConstraint(name = "uk_factor_board_name", columnNames = {"decision_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
public class ComparisonFactor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false)
    private DecisionBoard board;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal weight;

    @OneToMany(mappedBy = "factor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionFactorScore> scores = new ArrayList<>();
}
