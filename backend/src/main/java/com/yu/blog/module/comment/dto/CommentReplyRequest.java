package com.yu.blog.module.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentReplyRequest(
        @NotBlank
        @Size(max = 1000)
        String adminReply
) {
}
