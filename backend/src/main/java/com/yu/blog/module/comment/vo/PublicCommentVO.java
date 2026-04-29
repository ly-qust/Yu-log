package com.yu.blog.module.comment.vo;

import com.yu.blog.module.comment.entity.Comment;
import java.time.LocalDateTime;

public record PublicCommentVO(
        String id,
        String nickname,
        String content,
        LocalDateTime createdAt,
        String adminReply,
        LocalDateTime repliedAt
) {
    public static PublicCommentVO from(Comment comment) {
        return new PublicCommentVO(
                String.valueOf(comment.getId()),
                comment.getNickname(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getAdminReply(),
                comment.getRepliedAt()
        );
    }
}
