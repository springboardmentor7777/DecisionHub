package com.decisionhub.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "board_votes")
public class BoardVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private DecisionBoard board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private DecisionOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private boolean anonymous;

    @Column
    private Integer rating;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected BoardVote() {}

    public BoardVote(DecisionBoard board, DecisionOption option, AppUser user, boolean anonymous, Integer rating) {
        this.board = board;
        this.option = option;
        this.user = user;
        this.anonymous = anonymous;
        this.rating = rating;
    }

    public Long getId() { return id; }
    public DecisionBoard getBoard() { return board; }
    public DecisionOption getOption() { return option; }
    public AppUser getUser() { return user; }
    public Integer getRating() { return rating; }
    public Instant getCreatedAt() { return createdAt; }
}
