package com.decisionhub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decision_boards")
public class DecisionBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 3000)
    private String description;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PollType pollType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DecisionOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardVote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardComment> comments = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected DecisionBoard() {}

    public DecisionBoard(String title, String description, String category, BoardVisibility visibility, PollType pollType, AppUser owner) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.visibility = visibility;
        this.pollType = pollType;
        this.owner = owner;
    }

    public void addOption(DecisionOption option) {
        options.add(option);
        option.setBoard(this);
    }

    public void update(String title, String description, String category, BoardVisibility visibility, PollType pollType) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.visibility = visibility;
        this.pollType = pollType;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BoardVisibility getVisibility() { return visibility; }
    public PollType getPollType() { return pollType; }
    public AppUser getOwner() { return owner; }
    public List<DecisionOption> getOptions() { return options; }
    public Instant getCreatedAt() { return createdAt; }
}
