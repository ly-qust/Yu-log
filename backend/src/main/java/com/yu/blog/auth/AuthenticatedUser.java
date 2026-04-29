package com.yu.blog.auth;

public record AuthenticatedUser(
        Long id,
        String username,
        String roleCode
) {
}
