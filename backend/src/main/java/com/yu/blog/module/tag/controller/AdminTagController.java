package com.yu.blog.module.tag.controller;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.tag.dto.TagSaveRequest;
import com.yu.blog.module.tag.service.TagService;
import com.yu.blog.module.tag.vo.AdminTagVO;
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
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {
    private final TagService tagService;

    @GetMapping
    public Result<List<AdminTagVO>> list() {
        return Result.ok(tagService.listAdminTags());
    }

    @PostMapping
    public Result<AdminTagVO> create(@Valid @RequestBody TagSaveRequest request) {
        return Result.ok(tagService.create(request));
    }

    @PutMapping("/{id}")
    public Result<AdminTagVO> update(@PathVariable Long id, @Valid @RequestBody TagSaveRequest request) {
        return Result.ok(tagService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok();
    }
}
