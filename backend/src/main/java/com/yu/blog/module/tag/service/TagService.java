package com.yu.blog.module.tag.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yu.blog.common.cache.CacheKeys;
import com.yu.blog.common.cache.CacheProperties;
import com.yu.blog.common.cache.CacheService;
import com.yu.blog.common.exception.BusinessException;
import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.article.entity.ArticleTag;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.article.mapper.ArticleTagMapper;
import com.yu.blog.module.tag.dto.TagSaveRequest;
import com.yu.blog.module.tag.entity.Tag;
import com.yu.blog.module.tag.mapper.TagMapper;
import com.yu.blog.module.tag.vo.AdminTagVO;
import com.yu.blog.module.tag.vo.TagVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {
    private static final String ENABLED = "ENABLED";
    private static final String PUBLISHED = "PUBLISHED";

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleMapper articleMapper;
    private final CacheService cacheService;
    private final CacheProperties cacheProperties;

    public List<TagVO> listPublicTags() {
        return cacheService.getList(CacheKeys.tagList(), TagVO.class)
                .orElseGet(() -> {
                    List<TagVO> tags = loadPublicTags();
                    cacheService.put(CacheKeys.tagList(), tags, cacheProperties.tagTtl());
                    return tags;
                });
    }

    private List<TagVO> loadPublicTags() {
        return tagMapper.selectList(Wrappers.lambdaQuery(Tag.class)
                        .eq(Tag::getStatus, ENABLED)
                        .orderByAsc(Tag::getName)
                        .orderByAsc(Tag::getId))
                .stream()
                .map(tag -> TagVO.from(tag, countArticles(tag.getId(), true)))
                .toList();
    }

    public List<AdminTagVO> listAdminTags() {
        return tagMapper.selectList(Wrappers.lambdaQuery(Tag.class)
                        .orderByDesc(Tag::getId))
                .stream()
                .map(tag -> AdminTagVO.from(tag, countArticles(tag.getId(), false)))
                .toList();
    }

    @Transactional
    public AdminTagVO create(TagSaveRequest request) {
        ensureSlugAvailable(request.slug(), null);
        ensureNameAvailable(request.name(), null);

        Tag tag = new Tag();
        tag.setName(request.name());
        tag.setSlug(request.slug());
        tag.setColor(request.color());
        tag.setDescription(request.description());
        tag.setStatus(defaultStatus(request.status()));
        tagMapper.insert(tag);
        invalidateTagCaches();
        return AdminTagVO.from(tag, 0);
    }

    @Transactional
    public AdminTagVO update(Long id, TagSaveRequest request) {
        Tag tag = getExisting(id);
        ensureSlugAvailable(request.slug(), id);
        ensureNameAvailable(request.name(), id);

        tag.setName(request.name());
        tag.setSlug(request.slug());
        tag.setColor(request.color());
        tag.setDescription(request.description());
        tag.setStatus(defaultStatus(request.status()));
        tagMapper.updateById(tag);
        Tag updated = tagMapper.selectById(id);
        invalidateTagCaches();
        return AdminTagVO.from(updated, countArticles(id, false));
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = getExisting(id);
        Long relationCount = articleTagMapper.selectCount(Wrappers.lambdaQuery(ArticleTag.class)
                .eq(ArticleTag::getTagId, tag.getId()));
        if (relationCount > 0) {
            throw new BusinessException(400, "标签已被文章使用，不能删除");
        }
        tagMapper.deleteById(id);
        invalidateTagCaches();
    }

    public List<Tag> getEnabledTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        List<Long> distinctIds = tagIds.stream().distinct().toList();
        List<Tag> tags = tagMapper.selectList(Wrappers.lambdaQuery(Tag.class)
                .in(Tag::getId, distinctIds)
                .eq(Tag::getStatus, ENABLED));
        if (tags.size() != distinctIds.size()) {
            throw new BusinessException(400, "存在不存在或未启用的标签");
        }
        return tags;
    }

    private Tag getExisting(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(404, "标签不存在");
        }
        return tag;
    }

    private void ensureSlugAvailable(String slug, Long excludeId) {
        Long count = tagMapper.selectCount(Wrappers.lambdaQuery(Tag.class)
                .eq(Tag::getSlug, slug)
                .ne(excludeId != null, Tag::getId, excludeId));
        if (count > 0) {
            throw new BusinessException(400, "标签 slug 已存在");
        }
    }

    private void ensureNameAvailable(String name, Long excludeId) {
        Long count = tagMapper.selectCount(Wrappers.lambdaQuery(Tag.class)
                .eq(Tag::getName, name)
                .ne(excludeId != null, Tag::getId, excludeId));
        if (count > 0) {
            throw new BusinessException(400, "标签名称已存在");
        }
    }

    private long countArticles(Long tagId, boolean publishedOnly) {
        List<Long> articleIds = articleTagMapper.selectList(Wrappers.lambdaQuery(ArticleTag.class)
                        .eq(ArticleTag::getTagId, tagId))
                .stream()
                .map(ArticleTag::getArticleId)
                .distinct()
                .toList();
        if (articleIds.isEmpty()) {
            return 0;
        }
        return articleMapper.selectCount(Wrappers.lambdaQuery(Article.class)
                .in(Article::getId, articleIds)
                .eq(publishedOnly, Article::getStatus, PUBLISHED));
    }

    private String defaultStatus(String status) {
        return status == null || status.isBlank() ? ENABLED : status;
    }

    private void invalidateTagCaches() {
        cacheService.evict(CacheKeys.tagList());
        cacheService.evict(CacheKeys.homeOverview());
        cacheService.evictByPattern(CacheKeys.articleListPattern());
    }
}
