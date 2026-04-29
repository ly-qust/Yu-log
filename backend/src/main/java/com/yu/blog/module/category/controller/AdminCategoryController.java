package com.yu.blog.module.category.controller;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.category.dto.CategorySaveRequest;
import com.yu.blog.module.category.service.CategoryService;
import com.yu.blog.module.category.vo.AdminCategoryVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Result<List<AdminCategoryVO>> list() {
        return Result.ok(categoryService.listAdminCategories());
    }

    @PostMapping
    public Result<AdminCategoryVO> create(@Valid @RequestBody CategorySaveRequest request) {
        return Result.ok(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public Result<AdminCategoryVO> update(@PathVariable Long id, @Valid @RequestBody CategorySaveRequest request) {
        return Result.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
