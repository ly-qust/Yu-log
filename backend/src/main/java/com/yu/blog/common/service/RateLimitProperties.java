package com.yu.blog.common.service;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yu-log.rate-limit")
public record RateLimitProperties(
        int loginIpMaxRequests,
        int loginUsernameMaxRequests,
        long loginWindowSeconds
) {
    public Duration loginWindow() {
        return Duration.ofSeconds(Math.max(1, loginWindowSeconds));
    }
}
