package com.yu.blog.module.comment.vo;

import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.comment.entity.Comment;
import java.time.LocalDateTime;

public record AdminCommentVO(
        String id,
        String articleId,
        String articleTitle,
        String nickname,
        String email,
        String content,
        String status,
        String adminReply,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime repliedAt
) {
    public static AdminCommentVO from(Comment comment, Article article) {
        return new AdminCommentVO(
                String.valueOf(comment.getId()),
                String.valueOf(comment.getArticleId()),
                article == null ? null : article.getTitle(),
                comment.getNickname(),
                comment.getEmail(),
                comment.getContent(),
                comment.getStatus(),
                comment.getAdminReply(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getRepliedAt()
        );
    }
}
