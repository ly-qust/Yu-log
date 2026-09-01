package com.yu.blog.module.comment.controller;

import com.yu.blog.common.api.Result;
import com.yu.blog.config.ClientIpResolver;
import com.yu.blog.module.comment.dto.CommentSubmitRequest;
import com.yu.blog.module.comment.service.CommentService;
import com.yu.blog.module.comment.vo.PublicCommentVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final ClientIpResolver clientIpResolver;

    @PostMapping
    public Result<String> submit(
            @PathVariable Long articleId,
            @Valid @RequestBody CommentSubmitRequest request,
            HttpServletRequest servletRequest
    ) {
        return Result.ok(commentService.submit(articleId, request, clientIpResolver.resolve(servletRequest), servletRequest.getHeader("User-Agent")));
    }

    @GetMapping
    public Result<List<PublicCommentVO>> list(@PathVariable Long articleId) {
        return Result.ok(commentService.listPublic(articleId));
    }
}
