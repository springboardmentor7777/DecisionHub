package com.decisionhub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "decision_options")
public class DecisionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private DecisionBoard board;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 3000)
    private String pros;

    @Column(length = 3000)
    private String cons;

    @Column
    private Integer score;

    protected DecisionOption() {}

    public DecisionOption(String name, String description, String pros, String cons, Integer score) {
        this.name = name;
        this.description = description;
        this.pros = pros;
        this.cons = cons;
        this.score = score;
    }

    public void setBoard(DecisionBoard board) { this.board = board; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPros() { return pros; }
    public String getCons() { return cons; }
    public Integer getScore() { return score; }
}
