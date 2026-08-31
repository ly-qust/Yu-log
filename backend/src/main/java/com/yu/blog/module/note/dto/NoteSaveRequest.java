package com.yu.blog.module.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record NoteSaveRequest(
        @NotBlank
        @Size(max = 180)
        String title,

        @NotBlank
        @Size(max = 180)
        String slug,

        @Size(max = 512)
        String summary,

        @NotBlank
        String content,

        @Size(max = 64)
        String topic,

        List<String> tags,

        Boolean isPublic,

        Integer sortOrder
) {
}
