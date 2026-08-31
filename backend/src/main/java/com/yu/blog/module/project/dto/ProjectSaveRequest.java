package com.yu.blog.module.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProjectSaveRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotBlank
        @Size(max = 160)
        String slug,

        @Size(max = 1024)
        String description,

        String detailContent,

        @Size(max = 512)
        String coverImage,

        List<String> techStack,

        @NotBlank
        String status,

        @Size(max = 512)
        String githubUrl,

        @Size(max = 512)
        String demoUrl,

        Integer sortOrder,

        Boolean visible
) {
}
