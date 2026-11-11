package com.decisionhub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "board_comments")
public class BoardComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private DecisionBoard board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BoardComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<BoardComment> replies = new ArrayList<>();

    @Column(nullable = false, length = 3000)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected BoardComment() {}

    public BoardComment(DecisionBoard board, AppUser author, BoardComment parent, String content) {
        this.board = board;
        this.author = author;
        this.parent = parent;
        this.content = content;
    }

    public Long getId() { return id; }
    public DecisionBoard getBoard() { return board; }
    public AppUser getAuthor() { return author; }
    public BoardComment getParent() { return parent; }
    public List<BoardComment> getReplies() { return replies; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
