package com.yu.blog.common.service;

import com.yu.blog.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {
    private static final String TOO_FREQUENT_MESSAGE = "\u63d0\u4ea4\u8fc7\u4e8e\u9891\u7e41\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5";

    private final StringRedisTemplate redisTemplate;
    private final Map<String, MemoryCounter> memoryCounters = new ConcurrentHashMap<>();

    public void check(String scope, String clientIp, int maxRequests, Duration window) {
        String key = "rate:" + scope + ":" + sha256(clientIp == null ? "unknown" : clientIp);
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, window);
            }
            if (count != null && count > maxRequests) {
                throw new BusinessException(429, TOO_FREQUENT_MESSAGE);
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("Redis rate limit unavailable, fallback to in-memory counter. scope={}", scope, exception);
            checkInMemory(key, maxRequests, window);
        }
    }

    public void checkLogin(String clientIp, String username, RateLimitProperties properties) {
        check("login-ip", clientIp, properties.loginIpMaxRequests(), properties.loginWindow());
        String normalizedUsername = username == null ? "unknown" : username.trim().toLowerCase(Locale.ROOT);
        check("login-user", normalizedUsername, properties.loginUsernameMaxRequests(), properties.loginWindow());
    }

    public String ipHash(String clientIp) {
        return sha256(clientIp == null ? "unknown" : clientIp);
    }

    private synchronized void checkInMemory(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        MemoryCounter counter = memoryCounters.get(key);
        if (counter == null || now - counter.windowStartMillis >= window.toMillis()) {
            memoryCounters.put(key, new MemoryCounter(now, 1));
            return;
        }
        counter.count++;
        if (counter.count > maxRequests) {
            throw new BusinessException(429, TOO_FREQUENT_MESSAGE);
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static class MemoryCounter {
        private final long windowStartMillis;
        private int count;

        private MemoryCounter(long windowStartMillis, int count) {
            this.windowStartMillis = windowStartMillis;
            this.count = count;
        }
    }
}
