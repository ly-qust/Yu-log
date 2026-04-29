package com.yu.blog.module.article.vo;

import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.category.entity.Category;
import com.yu.blog.module.tag.vo.TagOptionVO;
import java.time.LocalDateTime;
import java.util.List;

public record AdminArticleDetailVO(
        String id,
        String title,
        String slug,
        String summary,
        String content,
        String coverImage,
        String categoryId,
        String categoryName,
        List<TagOptionVO> tags,
        String status,
        Boolean isTop,
        long viewCount,
        long likeCount,
        long commentCount,
        int readingTime,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminArticleDetailVO from(Article article, Category category, List<TagOptionVO> tags) {
        return new AdminArticleDetailVO(
                String.valueOf(article.getId()),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getContentMd(),
                article.getCoverImageUrl(),
                article.getCategoryId() == null ? null : String.valueOf(article.getCategoryId()),
                category == null ? null : category.getName(),
                tags == null ? List.of() : tags,
                article.getStatus(),
                article.getIsTop(),
                ArticleListVO.safeLong(article.getViewCount()),
                ArticleListVO.safeLong(article.getLikeCount()),
                ArticleListVO.safeLong(article.getCommentCount()),
                ArticleListVO.safeInt(article.getReadingTime()),
                article.getPublishedAt(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }
}
