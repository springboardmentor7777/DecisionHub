package com.decisionhub.service;

import com.decisionhub.exception.BadRequestException;

import java.util.Arrays;
import java.util.List;

public final class CategoryCatalog {
    private static final List<String> CATEGORIES = List.of(
            "Career", "Education", "Technology", "Travel", "Finance", "Lifestyle");

    private CategoryCatalog() {}

    public static String normalize(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return CATEGORIES.stream()
                .filter(value -> value.equalsIgnoreCase(category.trim()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Category must be one of: " + String.join(", ", CATEGORIES)));
    }

    public static List<String> all() {
        return CATEGORIES;
    }
}
