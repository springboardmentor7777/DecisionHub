package com.decisionhub.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "communities")
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private CommunityCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Community() {}

    public Community(String name, String description, CommunityCategory category, AppUser owner) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public CommunityCategory getCategory() { return category; }
    public AppUser getOwner() { return owner; }
    public Instant getCreatedAt() { return createdAt; }
}
