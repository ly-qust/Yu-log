package com.yu.blog.module.article.controller;

import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.api.Result;
import com.yu.blog.module.article.dto.ArticleSaveRequest;
import com.yu.blog.module.article.dto.ArticleStatusRequest;
import com.yu.blog.module.article.dto.ArticleTopRequest;
import com.yu.blog.module.article.service.ArticleService;
import com.yu.blog.module.article.vo.AdminArticleDetailVO;
import com.yu.blog.module.article.vo.AdminArticleListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {
    private final ArticleService articleService;

    @GetMapping
    public Result<PageResult<AdminArticleListVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(articleService.listAdminArticles(keyword, categoryId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<AdminArticleDetailVO> detail(@PathVariable Long id) {
        return Result.ok(articleService.getAdminDetail(id));
    }

    @PostMapping
    public Result<AdminArticleDetailVO> create(@Valid @RequestBody ArticleSaveRequest request) {
        return Result.ok(articleService.create(request));
    }

    @PutMapping("/{id}")
    public Result<AdminArticleDetailVO> update(@PathVariable Long id, @Valid @RequestBody ArticleSaveRequest request) {
        return Result.ok(articleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<AdminArticleDetailVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ArticleStatusRequest request
    ) {
        return Result.ok(articleService.updateStatus(id, request.status()));
    }

    @PutMapping("/{id}/top")
    public Result<AdminArticleDetailVO> updateTop(
            @PathVariable Long id,
            @Valid @RequestBody ArticleTopRequest request
    ) {
        return Result.ok(articleService.updateTop(id, request.isTop()));
    }
}
