package com.yu.blog.auth.vo;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserInfoResponse user
) {
}
