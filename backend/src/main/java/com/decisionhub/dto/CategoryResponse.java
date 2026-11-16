package com.decisionhub.dto;

import com.decisionhub.entity.Category;

public record CategoryResponse(Long id, String name) {
    public static CategoryResponse from(Category c) { return new CategoryResponse(c.getId(), c.getName()); }
}