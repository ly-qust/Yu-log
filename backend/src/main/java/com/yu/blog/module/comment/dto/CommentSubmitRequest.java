package com.yu.blog.module.comment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentSubmitRequest(
        @NotBlank
        @Size(min = 2, max = 30)
        String nickname,

        @Email
        @Size(max = 128)
        String email,

        @NotBlank
        @Size(max = 1000)
        String content
) {
}
