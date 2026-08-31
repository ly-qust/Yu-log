package com.yu.blog.module.project.controller;

import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.api.Result;
import com.yu.blog.module.project.dto.ProjectSaveRequest;
import com.yu.blog.module.project.service.ProjectService;
import com.yu.blog.module.project.vo.ProjectDetailVO;
import com.yu.blog.module.project.vo.ProjectVO;
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
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {
    private final ProjectService projectService;

    @GetMapping
    public Result<PageResult<ProjectVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(projectService.listAdmin(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<ProjectDetailVO> detail(@PathVariable Long id) {
        return Result.ok(projectService.getAdminDetail(id));
    }

    @PostMapping
    public Result<ProjectDetailVO> create(@Valid @RequestBody ProjectSaveRequest request) {
        return Result.ok(projectService.create(request));
    }

    @PutMapping("/{id}")
    public Result<ProjectDetailVO> update(@PathVariable Long id, @Valid @RequestBody ProjectSaveRequest request) {
        return Result.ok(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.ok();
    }
}
