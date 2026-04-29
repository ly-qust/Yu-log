package com.yu.blog.module.article.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.blog.auth.AuthenticatedUser;
import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.exception.BusinessException;
import com.yu.blog.module.article.dto.ArticleSaveRequest;
import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.article.entity.ArticleTag;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.article.mapper.ArticleTagMapper;
import com.yu.blog.module.article.vo.AdminArticleDetailVO;
import com.yu.blog.module.article.vo.AdminArticleListVO;
import com.yu.blog.module.article.vo.ArticleDetailVO;
import com.yu.blog.module.article.vo.ArticleLikeVO;
import com.yu.blog.module.article.vo.ArticleListVO;
import com.yu.blog.module.category.entity.Category;
import com.yu.blog.module.category.mapper.CategoryMapper;
import com.yu.blog.module.category.service.CategoryService;
import com.yu.blog.module.tag.entity.Tag;
import com.yu.blog.module.tag.mapper.TagMapper;
import com.yu.blog.module.tag.service.TagService;
import com.yu.blog.module.tag.vo.TagOptionVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String HIDDEN = "HIDDEN";

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CategoryService categoryService;
    private final TagService tagService;

    public PageResult<ArticleListVO> listPublicArticles(
            String keyword,
            Long categoryId,
            Long tagId,
            long page,
            long size,
            String sort
    ) {
        List<Long> tagArticleIds = articleIdsByTag(tagId);
        if (tagId != null && tagArticleIds.isEmpty()) {
            return PageResult.empty(page, size);
        }

        LambdaQueryWrapper<Article> query = Wrappers.lambdaQuery(Article.class)
                .eq(Article::getStatus, PUBLISHED)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .in(tagId != null, Article::getId, tagArticleIds)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(Article::getTitle, keyword)
                        .or()
                        .like(Article::getSummary, keyword));
        applyPublicSort(query, sort);

        IPage<Article> result = articleMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(toPublicList(result.getRecords()), result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Transactional
    public ArticleDetailVO getPublicDetail(Long id) {
        Article article = articleMapper.selectOne(Wrappers.lambdaQuery(Article.class)
                .eq(Article::getId, id)
                .eq(Article::getStatus, PUBLISHED));
        if (article == null) {
            throw new BusinessException(404, "文章不存在或未发布");
        }
        articleMapper.update(null, Wrappers.lambdaUpdate(Article.class)
                .eq(Article::getId, id)
                .setSql("view_count = view_count + 1"));
        Article updated = articleMapper.selectById(id);
        return toPublicDetail(updated);
    }

    @Transactional
    public ArticleLikeVO like(Long id) {
        Article article = articleMapper.selectOne(Wrappers.lambdaQuery(Article.class)
                .eq(Article::getId, id)
                .eq(Article::getStatus, PUBLISHED));
        if (article == null) {
            throw new BusinessException(404, "文章不存在或未发布");
        }
        articleMapper.update(null, Wrappers.lambdaUpdate(Article.class)
                .eq(Article::getId, id)
                .setSql("like_count = like_count + 1"));
        Article updated = articleMapper.selectById(id);
        return new ArticleLikeVO(updated.getLikeCount() == null ? 0 : updated.getLikeCount());
    }

    public PageResult<AdminArticleListVO> listAdminArticles(
            String keyword,
            Long categoryId,
            String status,
            long page,
            long size
    ) {
        LambdaQueryWrapper<Article> query = Wrappers.lambdaQuery(Article.class)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .eq(StringUtils.hasText(status), Article::getStatus, status)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(Article::getTitle, keyword)
                        .or()
                        .like(Article::getSummary, keyword))
                .orderByDesc(Article::getCreatedAt)
                .orderByDesc(Article::getId);
        IPage<Article> result = articleMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(toAdminList(result.getRecords()), result.getCurrent(), result.getSize(), result.getTotal());
    }

    public AdminArticleDetailVO getAdminDetail(Long id) {
        return toAdminDetail(getExisting(id));
    }

    @Transactional
    public AdminArticleDetailVO create(ArticleSaveRequest request) {
        validateStatus(request.status());
        categoryService.getEnabledArticleCategory(request.categoryId());
        List<Tag> tags = tagService.getEnabledTags(request.tagIds());
        ensureSlugAvailable(request.slug(), null);

        Article article = new Article();
        article.setAuthorUserId(currentUserId());
        article.setCategoryId(request.categoryId());
        applyEditableFields(article, request);
        article.setViewCount(0L);
        article.setLikeCount(0L);
        article.setCommentCount(0L);
        article.setAllowComment(true);
        article.setPublishedAt(PUBLISHED.equals(request.status()) ? LocalDateTime.now() : null);
        articleMapper.insert(article);
        syncTags(article.getId(), tags);
        return toAdminDetail(articleMapper.selectById(article.getId()));
    }

    @Transactional
    public AdminArticleDetailVO update(Long id, ArticleSaveRequest request) {
        validateStatus(request.status());
        Article article = getExisting(id);
        categoryService.getEnabledArticleCategory(request.categoryId());
        List<Tag> tags = tagService.getEnabledTags(request.tagIds());
        ensureSlugAvailable(request.slug(), id);

        boolean shouldPublishNow = PUBLISHED.equals(request.status()) && article.getPublishedAt() == null;
        applyEditableFields(article, request);
        if (shouldPublishNow) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.updateById(article);
        syncTags(id, tags);
        return toAdminDetail(articleMapper.selectById(id));
    }

    @Transactional
    public void delete(Long id) {
        getExisting(id);
        articleMapper.deleteById(id);
        articleTagMapper.delete(Wrappers.lambdaQuery(ArticleTag.class).eq(ArticleTag::getArticleId, id));
    }

    @Transactional
    public AdminArticleDetailVO updateStatus(Long id, String status) {
        validateStatus(status);
        Article article = getExisting(id);
        if (PUBLISHED.equals(status) && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        article.setStatus(status);
        articleMapper.updateById(article);
        return toAdminDetail(articleMapper.selectById(id));
    }

    @Transactional
    public AdminArticleDetailVO updateTop(Long id, Boolean isTop) {
        Article article = getExisting(id);
        article.setIsTop(Boolean.TRUE.equals(isTop));
        articleMapper.updateById(article);
        return toAdminDetail(articleMapper.selectById(id));
    }

    private void applyEditableFields(Article article, ArticleSaveRequest request) {
        article.setTitle(request.title());
        article.setSlug(request.slug());
        article.setSummary(request.summary());
        article.setContentMd(request.content());
        article.setCoverImageUrl(request.coverImage());
        article.setCategoryId(request.categoryId());
        article.setStatus(request.status());
        article.setIsTop(Boolean.TRUE.equals(request.isTop()));
        article.setReadingTime(resolveReadingTime(request.readingTime(), request.content()));
    }

    private void syncTags(Long articleId, List<Tag> tags) {
        articleTagMapper.delete(Wrappers.lambdaQuery(ArticleTag.class).eq(ArticleTag::getArticleId, articleId));
        tags.stream()
                .map(Tag::getId)
                .distinct()
                .forEach(tagId -> {
                    ArticleTag articleTag = new ArticleTag();
                    articleTag.setArticleId(articleId);
                    articleTag.setTagId(tagId);
                    articleTagMapper.insert(articleTag);
                });
    }

    private Article getExisting(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        return article;
    }

    private void ensureSlugAvailable(String slug, Long excludeId) {
        Long count = articleMapper.selectCount(Wrappers.lambdaQuery(Article.class)
                .eq(Article::getSlug, slug)
                .ne(excludeId != null, Article::getId, excludeId));
        if (count > 0) {
            throw new BusinessException(400, "文章 slug 已存在");
        }
    }

    private void validateStatus(String status) {
        if (!DRAFT.equals(status) && !PUBLISHED.equals(status) && !HIDDEN.equals(status)) {
            throw new BusinessException(400, "文章状态不合法");
        }
    }

    private List<Long> articleIdsByTag(Long tagId) {
        if (tagId == null) {
            return List.of();
        }
        return articleTagMapper.selectList(Wrappers.lambdaQuery(ArticleTag.class).eq(ArticleTag::getTagId, tagId))
                .stream()
                .map(ArticleTag::getArticleId)
                .distinct()
                .toList();
    }

    private List<ArticleListVO> toPublicList(List<Article> articles) {
        Map<Long, Category> categories = categoriesById(articles);
        return articles.stream()
                .map(article -> ArticleListVO.from(article, categories.get(article.getCategoryId()), tagsForArticle(article.getId())))
                .toList();
    }

    private List<AdminArticleListVO> toAdminList(List<Article> articles) {
        Map<Long, Category> categories = categoriesById(articles);
        return articles.stream()
                .map(article -> AdminArticleListVO.from(article, categories.get(article.getCategoryId()), tagsForArticle(article.getId())))
                .toList();
    }

    private ArticleDetailVO toPublicDetail(Article article) {
        return ArticleDetailVO.from(article, categoryMapper.selectById(article.getCategoryId()), tagsForArticle(article.getId()));
    }

    private AdminArticleDetailVO toAdminDetail(Article article) {
        return AdminArticleDetailVO.from(article, categoryMapper.selectById(article.getCategoryId()), tagsForArticle(article.getId()));
    }

    private List<TagOptionVO> tagsForArticle(Long articleId) {
        List<Long> tagIds = articleTagMapper.selectList(Wrappers.lambdaQuery(ArticleTag.class)
                        .eq(ArticleTag::getArticleId, articleId))
                .stream()
                .map(ArticleTag::getTagId)
                .distinct()
                .toList();
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectBatchIds(tagIds).stream()
                .map(TagOptionVO::from)
                .toList();
    }

    private Map<Long, Category> categoriesById(List<Article> articles) {
        List<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    private void applyPublicSort(LambdaQueryWrapper<Article> query, String sort) {
        query.orderByDesc(Article::getIsTop);
        if ("views".equalsIgnoreCase(sort)) {
            query.orderByDesc(Article::getViewCount);
        } else if ("likes".equalsIgnoreCase(sort)) {
            query.orderByDesc(Article::getLikeCount);
        } else if ("oldest".equalsIgnoreCase(sort)) {
            query.orderByAsc(Article::getPublishedAt);
        } else {
            query.orderByDesc(Article::getPublishedAt);
        }
        query.orderByDesc(Article::getId);
    }

    private int resolveReadingTime(Integer readingTime, String content) {
        if (readingTime != null && readingTime > 0) {
            return readingTime;
        }
        int length = content == null ? 0 : content.length();
        return Math.max(1, (int) Math.ceil(length / 500.0));
    }

    private long safePage(long page) {
        return page <= 0 ? 1 : page;
    }

    private long safeSize(long size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.id();
        }
        return 1L;
    }
}
