package com.yu.blog.module.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategorySaveRequest(
        @Pattern(regexp = "ARTICLE|NOTE") String bizType,
        @NotBlank @Size(max = 64) String name,
        @NotBlank @Size(max = 120) String slug,
        @Size(max = 512) String description,
        Integer sortOrder,
        @Pattern(regexp = "ENABLED|DISABLED") String status
) {
}
