package com.yu.blog.module.article.dto;

import jakarta.validation.constraints.NotNull;

public record ArticleTopRequest(
        @NotNull Boolean isTop
) {
}
