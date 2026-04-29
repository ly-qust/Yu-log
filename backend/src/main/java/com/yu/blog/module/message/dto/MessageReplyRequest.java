package com.yu.blog.module.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageReplyRequest(
        @NotBlank
        @Size(max = 1000)
        String adminReply
) {
}
