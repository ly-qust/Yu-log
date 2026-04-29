package com.yu.blog.module.message.vo;

import com.yu.blog.module.message.entity.Message;
import java.time.LocalDateTime;

public record PublicMessageVO(
        String id,
        String nickname,
        String content,
        String adminReply,
        LocalDateTime createdAt,
        LocalDateTime repliedAt
) {
    public static PublicMessageVO from(Message message) {
        return new PublicMessageVO(
                String.valueOf(message.getId()),
                message.getNickname(),
                message.getContent(),
                message.getReplyContent(),
                message.getCreatedAt(),
                message.getRepliedAt()
        );
    }
}
