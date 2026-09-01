package com.yu.blog.module.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.blog.config.ClientIpResolver;
import com.yu.blog.common.service.RateLimitService;
import com.yu.blog.module.log.entity.OperationLog;
import com.yu.blog.module.log.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {
    public static final String OPERATOR_USER_ID_ATTRIBUTE = "yu-log.operator-user-id";

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;
    private final ClientIpResolver clientIpResolver;
    private final RateLimitService rateLimitService;

    public void record(HttpServletRequest request, String module, String action, Long bizId, int responseStatus) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setOperatorUserId(operatorUserId(request));
            operationLog.setModule(module);
            operationLog.setAction(action);
            operationLog.setBizId(bizId);
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setRequestUri(request.getRequestURI());
            operationLog.setRequestJson(json(Map.of(
                    "method", request.getMethod(),
                    "uri", request.getRequestURI()
            )));
            operationLog.setResponseJson(json(Map.of("status", responseStatus)));
            operationLog.setIp(rateLimitService.ipHash(clientIpResolver.resolve(request)));
            operationLog.setUserAgent(truncate(request.getHeader("User-Agent"), 512));
            operationLog.setSuccess(responseStatus >= 200 && responseStatus < 400);
            operationLogMapper.insert(operationLog);
        } catch (Exception exception) {
            log.warn("Operation log write failed. module={}, action={}", module, action, exception);
        }
    }

    private Long operatorUserId(HttpServletRequest request) {
        Object requestUserId = request.getAttribute(OPERATOR_USER_ID_ATTRIBUTE);
        if (requestUserId instanceof Long userId) {
            return userId;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.yu.blog.auth.AuthenticatedUser user) {
            return user.id();
        }
        return null;
    }

    private String json(Map<String, Object> value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new LinkedHashMap<>(value));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
