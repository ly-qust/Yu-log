package com.yu.blog.module.tag.vo;

import com.yu.blog.module.tag.entity.Tag;

public record TagVO(
        String id,
        String name,
        String slug,
        String color,
        long articleCount
) {
    public static TagVO from(Tag tag, long articleCount) {
        return new TagVO(
                String.valueOf(tag.getId()),
                tag.getName(),
                tag.getSlug(),
                tag.getColor(),
                articleCount
        );
    }
}
