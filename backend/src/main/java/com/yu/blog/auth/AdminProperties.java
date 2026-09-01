package com.yu.blog.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yu-log.admin")
public record AdminProperties(
        String username,
        String password
) {
}
