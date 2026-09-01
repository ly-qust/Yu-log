package com.yu.blog;

import com.yu.blog.auth.JwtProperties;
import com.yu.blog.auth.AdminProperties;
import com.yu.blog.common.service.RateLimitProperties;
import com.yu.blog.config.TrustedProxyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, AdminProperties.class, RateLimitProperties.class, TrustedProxyProperties.class})
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
