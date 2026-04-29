package com.yu.blog.auth;

import com.yu.blog.module.user.entity.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE_CODE = "roleCode";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(SysUser user) {
        return generateToken(user, "access", properties.accessTokenExpireSeconds());
    }

    public String generateRefreshToken(SysUser user) {
        return generateToken(user, "refresh", properties.refreshTokenExpireSeconds());
    }

    public JwtClaims parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new JwtClaims(
                toLong(claims.get(CLAIM_USER_ID)),
                claims.get(CLAIM_USERNAME, String.class),
                claims.get(CLAIM_ROLE_CODE, String.class),
                claims.get(CLAIM_TOKEN_TYPE, String.class)
        );
    }

    public long accessTokenExpireSeconds() {
        return properties.accessTokenExpireSeconds();
    }

    private String generateToken(SysUser user, String tokenType, long expiresInSeconds) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expiresInSeconds);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE_CODE, user.getRoleCode())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
