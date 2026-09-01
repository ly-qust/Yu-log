package com.yu.blog.module.message.controller;

import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.api.Result;
import com.yu.blog.config.ClientIpResolver;
import com.yu.blog.module.message.dto.MessageSubmitRequest;
import com.yu.blog.module.message.service.MessageService;
import com.yu.blog.module.message.vo.PublicMessageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final ClientIpResolver clientIpResolver;

    @PostMapping
    public Result<String> submit(
            @Valid @RequestBody MessageSubmitRequest request,
            HttpServletRequest servletRequest
    ) {
        return Result.ok(messageService.submit(request, clientIpResolver.resolve(servletRequest), servletRequest.getHeader("User-Agent")));
    }

    @GetMapping
    public Result<PageResult<PublicMessageVO>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(messageService.listPublic(page, size));
    }
}
