package com.yu.blog.module.message.vo;

import com.yu.blog.module.message.entity.Message;
import java.time.LocalDateTime;

public record AdminMessageVO(
        String id,
        String nickname,
        String email,
        String content,
        String status,
        String adminReply,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime repliedAt
) {
    public static AdminMessageVO from(Message message) {
        return new AdminMessageVO(
                String.valueOf(message.getId()),
                message.getNickname(),
                message.getEmail(),
                message.getContent(),
                message.getStatus(),
                message.getReplyContent(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getRepliedAt()
        );
    }
}
