package com.yu.blog.module.category.controller;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.category.service.CategoryService;
import com.yu.blog.module.category.vo.CategoryVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> list() {
        return Result.ok(categoryService.listPublicCategories());
    }
}
