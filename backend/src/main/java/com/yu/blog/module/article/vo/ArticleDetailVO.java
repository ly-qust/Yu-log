package com.yu.blog.module.article.vo;

import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.category.entity.Category;
import com.yu.blog.module.tag.vo.TagOptionVO;
import java.time.LocalDateTime;
import java.util.List;

public record ArticleDetailVO(
        String id,
        String title,
        String slug,
        String summary,
        String content,
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
    public static ArticleDetailVO from(Article article, Category category, List<TagOptionVO> tags) {
        return new ArticleDetailVO(
                String.valueOf(article.getId()),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getContentMd(),
                article.getCoverImageUrl(),
                article.getCategoryId() == null ? null : String.valueOf(article.getCategoryId()),
                category == null ? null : category.getName(),
                tags == null ? List.of() : tags,
                ArticleListVO.safeLong(article.getViewCount()),
                ArticleListVO.safeLong(article.getLikeCount()),
                ArticleListVO.safeLong(article.getCommentCount()),
                ArticleListVO.safeInt(article.getReadingTime()),
                article.getIsTop(),
                article.getPublishedAt(),
                article.getUpdatedAt()
        );
    }
}
