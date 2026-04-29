package com.yu.blog.module.category.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yu.blog.common.exception.BusinessException;
import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.category.dto.CategorySaveRequest;
import com.yu.blog.module.category.entity.Category;
import com.yu.blog.module.category.mapper.CategoryMapper;
import com.yu.blog.module.category.vo.AdminCategoryVO;
import com.yu.blog.module.category.vo.CategoryVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private static final String ARTICLE_BIZ_TYPE = "ARTICLE";
    private static final String ENABLED = "ENABLED";
    private static final String PUBLISHED = "PUBLISHED";

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    public List<CategoryVO> listPublicCategories() {
        return categoryMapper.selectList(Wrappers.lambdaQuery(Category.class)
                        .eq(Category::getBizType, ARTICLE_BIZ_TYPE)
                        .eq(Category::getStatus, ENABLED)
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getId))
                .stream()
                .map(category -> CategoryVO.from(category, countArticles(category.getId(), true)))
                .toList();
    }

    public List<AdminCategoryVO> listAdminCategories() {
        return categoryMapper.selectList(Wrappers.lambdaQuery(Category.class)
                        .eq(Category::getBizType, ARTICLE_BIZ_TYPE)
                        .orderByAsc(Category::getSortOrder)
                        .orderByDesc(Category::getId))
                .stream()
                .map(category -> AdminCategoryVO.from(category, countArticles(category.getId(), false)))
                .toList();
    }

    @Transactional
    public AdminCategoryVO create(CategorySaveRequest request) {
        String bizType = defaultBizType(request.bizType());
        ensureSlugAvailable(bizType, request.slug(), null);

        Category category = new Category();
        category.setBizType(bizType);
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(defaultStatus(request.status()));
        categoryMapper.insert(category);
        return AdminCategoryVO.from(category, 0);
    }

    @Transactional
    public AdminCategoryVO update(Long id, CategorySaveRequest request) {
        Category category = getExisting(id);
        String bizType = defaultBizType(request.bizType());
        ensureSlugAvailable(bizType, request.slug(), id);

        category.setBizType(bizType);
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(defaultStatus(request.status()));
        categoryMapper.updateById(category);
        Category updated = categoryMapper.selectById(id);
        return AdminCategoryVO.from(updated, countArticles(id, false));
    }

    @Transactional
    public void delete(Long id) {
        Category category = getExisting(id);
        if (countArticles(category.getId(), false) > 0) {
            throw new BusinessException(400, "分类下已有文章，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    public Category getEnabledArticleCategory(Long id) {
        Category category = categoryMapper.selectOne(Wrappers.lambdaQuery(Category.class)
                .eq(Category::getId, id)
                .eq(Category::getBizType, ARTICLE_BIZ_TYPE)
                .eq(Category::getStatus, ENABLED));
        if (category == null) {
            throw new BusinessException(400, "分类不存在或未启用");
        }
        return category;
    }

    private Category getExisting(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return category;
    }

    private void ensureSlugAvailable(String bizType, String slug, Long excludeId) {
        Long count = categoryMapper.selectCount(Wrappers.lambdaQuery(Category.class)
                .eq(Category::getBizType, bizType)
                .eq(Category::getSlug, slug)
                .ne(excludeId != null, Category::getId, excludeId));
        if (count > 0) {
            throw new BusinessException(400, "分类 slug 已存在");
        }
    }

    private long countArticles(Long categoryId, boolean publishedOnly) {
        return articleMapper.selectCount(Wrappers.lambdaQuery(Article.class)
                .eq(Article::getCategoryId, categoryId)
                .eq(publishedOnly, Article::getStatus, PUBLISHED));
    }

    private String defaultBizType(String bizType) {
        return bizType == null || bizType.isBlank() ? ARTICLE_BIZ_TYPE : bizType;
    }

    private String defaultStatus(String status) {
        return status == null || status.isBlank() ? ENABLED : status;
    }
}
