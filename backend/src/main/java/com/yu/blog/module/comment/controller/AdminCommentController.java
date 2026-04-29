package com.yu.blog.module.comment.controller;

import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.api.Result;
import com.yu.blog.module.comment.dto.CommentReplyRequest;
import com.yu.blog.module.comment.dto.CommentStatusRequest;
import com.yu.blog.module.comment.service.CommentService;
import com.yu.blog.module.comment.vo.AdminCommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public Result<PageResult<AdminCommentVO>> list(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(commentService.listAdmin(articleId, status, keyword, page, size));
    }

    @PutMapping("/{id}/status")
    public Result<AdminCommentVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CommentStatusRequest request
    ) {
        return Result.ok(commentService.updateStatus(id, request.status()));
    }

    @PutMapping("/{id}/reply")
    public Result<AdminCommentVO> reply(
            @PathVariable Long id,
            @Valid @RequestBody CommentReplyRequest request
    ) {
        return Result.ok(commentService.reply(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.ok();
    }
}
