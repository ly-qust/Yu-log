package com.yu.blog.module.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagSaveRequest(
        @NotBlank @Size(max = 64) String name,
        @NotBlank @Size(max = 120) String slug,
        @Size(max = 32) String color,
        @Size(max = 512) String description,
        @Pattern(regexp = "ENABLED|DISABLED") String status
) {
}
