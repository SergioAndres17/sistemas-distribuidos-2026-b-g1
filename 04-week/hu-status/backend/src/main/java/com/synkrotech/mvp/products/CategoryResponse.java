package com.synkrotech.mvp.products;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, boolean active) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.isActive());
    }
}
