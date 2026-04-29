package com.yu.blog.module.article.vo;

import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.category.entity.Category;
import com.yu.blog.module.tag.vo.TagOptionVO;
import java.time.LocalDateTime;
import java.util.List;

public record AdminArticleListVO(
        String id,
        String title,
        String slug,
        String summary,
        String categoryId,
        String categoryName,
        List<TagOptionVO> tags,
        String status,
        Boolean isTop,
        long viewCount,
        long likeCount,
        long commentCount,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminArticleListVO from(Article article, Category category, List<TagOptionVO> tags) {
        return new AdminArticleListVO(
                String.valueOf(article.getId()),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getCategoryId() == null ? null : String.valueOf(article.getCategoryId()),
                category == null ? null : category.getName(),
                tags == null ? List.of() : tags,
                article.getStatus(),
                article.getIsTop(),
                ArticleListVO.safeLong(article.getViewCount()),
                ArticleListVO.safeLong(article.getLikeCount()),
                ArticleListVO.safeLong(article.getCommentCount()),
                article.getPublishedAt(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }
}
