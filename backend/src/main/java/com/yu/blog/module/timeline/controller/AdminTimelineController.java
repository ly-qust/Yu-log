package com.yu.blog.module.timeline.controller;

import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.api.Result;
import com.yu.blog.module.timeline.dto.TimelineSaveRequest;
import com.yu.blog.module.timeline.service.TimelineService;
import com.yu.blog.module.timeline.vo.TimelineEventVO;
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
@RequestMapping("/api/admin/timeline")
@RequiredArgsConstructor
public class AdminTimelineController {
    private final TimelineService timelineService;

    @GetMapping
    public Result<PageResult<TimelineEventVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(timelineService.listAdmin(keyword, type, page, size));
    }

    @GetMapping("/{id}")
    public Result<TimelineEventVO> detail(@PathVariable Long id) {
        return Result.ok(timelineService.getAdminDetail(id));
    }

    @PostMapping
    public Result<TimelineEventVO> create(@Valid @RequestBody TimelineSaveRequest request) {
        return Result.ok(timelineService.create(request));
    }

    @PutMapping("/{id}")
    public Result<TimelineEventVO> update(@PathVariable Long id, @Valid @RequestBody TimelineSaveRequest request) {
        return Result.ok(timelineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        timelineService.delete(id);
        return Result.ok();
    }
}
