package com.yu.blog.module.article.controller;

import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.api.Result;
import com.yu.blog.module.article.service.ArticleService;
import com.yu.blog.module.article.vo.ArticleDetailVO;
import com.yu.blog.module.article.vo.ArticleLikeVO;
import com.yu.blog.module.article.vo.ArticleListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping
    public Result<PageResult<ArticleListVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String sort
    ) {
        return Result.ok(articleService.listPublicArticles(keyword, categoryId, tagId, page, size, sort));
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.ok(articleService.getPublicDetail(id));
    }

    @PostMapping("/{id}/like")
    public Result<ArticleLikeVO> like(@PathVariable Long id) {
        return Result.ok(articleService.like(id));
    }
}
