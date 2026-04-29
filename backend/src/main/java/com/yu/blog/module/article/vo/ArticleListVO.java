package com.yu.blog.module.article.vo;

import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.category.entity.Category;
import com.yu.blog.module.tag.vo.TagOptionVO;
import java.time.LocalDateTime;
import java.util.List;

public record ArticleListVO(
        String id,
        String title,
        String slug,
        String summary,
        String coverImage,
        String categoryId,
        String categoryName,
        List<TagOptionVO> tags,
        long viewCount,
        long likeCount,
        long commentCount,
        int readingTime,
        Boolean isTop,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
    public static ArticleListVO from(Article article, Category category, List<TagOptionVO> tags) {
        return new ArticleListVO(
                String.valueOf(article.getId()),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getCoverImageUrl(),
                article.getCategoryId() == null ? null : String.valueOf(article.getCategoryId()),
                category == null ? null : category.getName(),
                tags == null ? List.of() : tags,
                safeLong(article.getViewCount()),
                safeLong(article.getLikeCount()),
                safeLong(article.getCommentCount()),
                safeInt(article.getReadingTime()),
                article.getIsTop(),
                article.getPublishedAt(),
                article.getUpdatedAt()
        );
    }

    static long safeLong(Long value) {
        return value == null ? 0 : value;
    }

    static int safeInt(Integer value) {
        return value == null ? 1 : value;
    }
}
