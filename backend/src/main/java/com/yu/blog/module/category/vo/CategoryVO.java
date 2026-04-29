package com.yu.blog.module.category.vo;

import com.yu.blog.module.category.entity.Category;

public record CategoryVO(
        String id,
        String name,
        String slug,
        String description,
        long articleCount,
        Integer sortOrder
) {
    public static CategoryVO from(Category category, long articleCount) {
        return new CategoryVO(
                String.valueOf(category.getId()),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                articleCount,
                category.getSortOrder()
        );
    }
}
