package com.yu.blog.module.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ArticleSaveRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 180) String slug,
        @Size(max = 512) String summary,
        @NotBlank String content,
        @Size(max = 512) String coverImage,
        @NotNull Long categoryId,
        List<Long> tagIds,
        @NotBlank @Pattern(regexp = "DRAFT|PUBLISHED|HIDDEN") String status,
        Boolean isTop,
        Integer readingTime
) {
}
