package com.decisionhub.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    USER,
    ADMIN,
    MODERATOR;

    @JsonValue
    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}