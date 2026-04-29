package com.yu.blog.module.tag.vo;

import com.yu.blog.module.tag.entity.Tag;

public record TagOptionVO(
        String id,
        String name,
        String slug,
        String color
) {
    public static TagOptionVO from(Tag tag) {
        return new TagOptionVO(
                String.valueOf(tag.getId()),
                tag.getName(),
                tag.getSlug(),
                tag.getColor()
        );
    }
}
