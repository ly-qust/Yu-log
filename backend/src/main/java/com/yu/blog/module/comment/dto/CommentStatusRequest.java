package com.yu.blog.module.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentStatusRequest(
        @NotBlank
        String status
) {
}
