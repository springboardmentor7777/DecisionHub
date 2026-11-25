package com.decisionhub.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** A record of a sensitive action (role change, content removal) for later review. */
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String actorEmail;

    @Column(name = "action_type", nullable = false, length = 60)
    private String action;

    @Column(nullable = false, length = 40)
    private String targetType;

    private Long targetId;

    @Column(length = 500)
    private String details;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AuditLog() {}

    public AuditLog(String actorEmail, String action, String targetType, Long targetId, String details) {
        this.actorEmail = actorEmail;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
    }

    public Long getId() { return id; }
    public String getActorEmail() { return actorEmail; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
