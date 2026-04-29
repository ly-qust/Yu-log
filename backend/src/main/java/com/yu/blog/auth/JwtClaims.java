package com.yu.blog.auth;

public record JwtClaims(
        Long userId,
        String username,
        String roleCode,
        String tokenType
) {
    public boolean isAccessToken() {
        return "access".equals(tokenType);
    }

    public boolean isRefreshToken() {
        return "refresh".equals(tokenType);
    }
}
