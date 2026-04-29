package com.yu.blog.module.tag.vo;

import com.yu.blog.module.tag.entity.Tag;
import java.time.LocalDateTime;

public record AdminTagVO(
        String id,
        String name,
        String slug,
        String color,
        String description,
        String status,
        long articleCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminTagVO from(Tag tag, long articleCount) {
        return new AdminTagVO(
                String.valueOf(tag.getId()),
                tag.getName(),
                tag.getSlug(),
                tag.getColor(),
                tag.getDescription(),
                tag.getStatus(),
                articleCount,
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }
}
