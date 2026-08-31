package com.yu.blog.module.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.blog.auth.AuthenticatedUser;
import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.cache.CacheKeys;
import com.yu.blog.common.cache.CacheService;
import com.yu.blog.common.exception.BusinessException;
import com.yu.blog.common.service.RateLimitService;
import com.yu.blog.module.message.dto.MessageReplyRequest;
import com.yu.blog.module.message.dto.MessageSubmitRequest;
import com.yu.blog.module.message.entity.Message;
import com.yu.blog.module.message.mapper.MessageMapper;
import com.yu.blog.module.message.vo.AdminMessageVO;
import com.yu.blog.module.message.vo.PublicMessageVO;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MessageService {
    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";
    private static final String SUBMITTED_MESSAGE = "\u7559\u8a00\u5df2\u63d0\u4ea4\uff0c\u5ba1\u6838\u901a\u8fc7\u540e\u5c55\u793a";

    private final MessageMapper messageMapper;
    private final RateLimitService rateLimitService;
    private final CacheService cacheService;

    @Transactional
    public String submit(MessageSubmitRequest request, String clientIp, String userAgent) {
        rateLimitService.check("message", clientIp, 3, Duration.ofSeconds(60));

        Message message = new Message();
        message.setNickname(trim(request.nickname()));
        message.setEmail(normalize(request.email()));
        message.setContent(trim(request.content()));
        message.setStatus(PENDING);
        message.setIpHash(rateLimitService.ipHash(clientIp));
        message.setUserAgent(truncate(userAgent, 512));
        messageMapper.insert(message);
        return SUBMITTED_MESSAGE;
    }

    public PageResult<PublicMessageVO> listPublic(long page, long size) {
        IPage<Message> result = messageMapper.selectPage(
                new Page<>(safePage(page), safeSize(size)),
                Wrappers.lambdaQuery(Message.class)
                        .eq(Message::getStatus, APPROVED)
                        .orderByDesc(Message::getCreatedAt)
                        .orderByDesc(Message::getId)
        );
        return PageResult.of(
                result.getRecords().stream().map(PublicMessageVO::from).toList(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        );
    }

    public PageResult<AdminMessageVO> listAdmin(String status, String keyword, long page, long size) {
        if (StringUtils.hasText(status)) {
            validateStatus(status);
        }
        LambdaQueryWrapper<Message> query = Wrappers.lambdaQuery(Message.class)
                .eq(StringUtils.hasText(status), Message::getStatus, status)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(Message::getNickname, keyword)
                        .or()
                        .like(Message::getEmail, keyword)
                        .or()
                        .like(Message::getContent, keyword))
                .orderByDesc(Message::getCreatedAt)
                .orderByDesc(Message::getId);
        IPage<Message> result = messageMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(
                result.getRecords().stream().map(AdminMessageVO::from).toList(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        );
    }

    @Transactional
    public AdminMessageVO updateStatus(Long id, String status) {
        validateStatus(status);
        Message message = getExisting(id);
        message.setStatus(status);
        messageMapper.updateById(message);
        cacheService.evict(CacheKeys.homeOverview());
        return AdminMessageVO.from(messageMapper.selectById(id));
    }

    @Transactional
    public AdminMessageVO reply(Long id, MessageReplyRequest request) {
        Message message = getExisting(id);
        message.setReplyContent(trim(request.adminReply()));
        message.setReplyUserId(currentUserId());
        message.setRepliedAt(LocalDateTime.now());
        messageMapper.updateById(message);
        return AdminMessageVO.from(messageMapper.selectById(id));
    }

    @Transactional
    public void delete(Long id) {
        getExisting(id);
        messageMapper.deleteById(id);
        cacheService.evict(CacheKeys.homeOverview());
    }

    private Message getExisting(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(404, "Message does not exist");
        }
        return message;
    }

    private void validateStatus(String status) {
        if (!PENDING.equals(status) && !APPROVED.equals(status) && !REJECTED.equals(status)) {
            throw new BusinessException(400, "Invalid message status");
        }
    }

    private long safePage(long page) {
        return page <= 0 ? 1 : page;
    }

    private long safeSize(long size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.id();
        }
        return 1L;
    }

    private String normalize(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
