package com.yu.blog.module.message.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageStatusRequest(
        @NotBlank
        String status
) {
}
