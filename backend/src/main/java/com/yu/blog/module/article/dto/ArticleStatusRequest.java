package com.yu.blog.module.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ArticleStatusRequest(
        @NotBlank @Pattern(regexp = "DRAFT|PUBLISHED|HIDDEN") String status
) {
}
