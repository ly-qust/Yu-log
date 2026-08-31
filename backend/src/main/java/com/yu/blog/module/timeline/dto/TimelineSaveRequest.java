package com.yu.blog.module.timeline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record TimelineSaveRequest(
        @NotBlank
        @Size(max = 180)
        String title,

        @Size(max = 512)
        String description,

        @NotNull
        LocalDate eventDate,

        @Size(max = 32)
        String type,

        List<String> tags,

        Integer sortOrder,

        Boolean visible
) {
}
