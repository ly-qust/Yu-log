package com.yu.blog.module.category.vo;

import com.yu.blog.module.category.entity.Category;
import java.time.LocalDateTime;

public record AdminCategoryVO(
        String id,
        String bizType,
        String name,
        String slug,
        String description,
        Integer sortOrder,
        String status,
        long articleCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminCategoryVO from(Category category, long articleCount) {
        return new AdminCategoryVO(
                String.valueOf(category.getId()),
                category.getBizType(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getSortOrder(),
                category.getStatus(),
                articleCount,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
