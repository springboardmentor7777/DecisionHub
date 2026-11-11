package com.decisionhub.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "community_memberships", uniqueConstraints = @UniqueConstraint(columnNames = {"community_id", "user_id"}))
public class CommunityMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    protected CommunityMembership() {}

    public CommunityMembership(Community community, AppUser user) {
        this.community = community;
        this.user = user;
    }

    public Long getId() { return id; }
    public Community getCommunity() { return community; }
    public AppUser getUser() { return user; }
    public Instant getJoinedAt() { return joinedAt; }
}
