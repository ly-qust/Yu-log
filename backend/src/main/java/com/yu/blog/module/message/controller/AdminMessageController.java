package com.yu.blog.module.message.controller;

import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.api.Result;
import com.yu.blog.module.message.dto.MessageReplyRequest;
import com.yu.blog.module.message.dto.MessageStatusRequest;
import com.yu.blog.module.message.service.MessageService;
import com.yu.blog.module.message.vo.AdminMessageVO;
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
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {
    private final MessageService messageService;

    @GetMapping
    public Result<PageResult<AdminMessageVO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(messageService.listAdmin(status, keyword, page, size));
    }

    @PutMapping("/{id}/status")
    public Result<AdminMessageVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody MessageStatusRequest request
    ) {
        return Result.ok(messageService.updateStatus(id, request.status()));
    }

    @PutMapping("/{id}/reply")
    public Result<AdminMessageVO> reply(
            @PathVariable Long id,
            @Valid @RequestBody MessageReplyRequest request
    ) {
        return Result.ok(messageService.reply(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        messageService.delete(id);
        return Result.ok();
    }
}
