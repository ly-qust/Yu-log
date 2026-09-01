package com.yu.blog.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yu.blog.auth.dto.LoginRequest;
import com.yu.blog.auth.dto.ChangePasswordRequest;
import com.yu.blog.auth.dto.RefreshTokenRequest;
import com.yu.blog.auth.vo.AuthTokenResponse;
import com.yu.blog.auth.vo.UserInfoResponse;
import com.yu.blog.common.exception.BusinessException;
import com.yu.blog.common.service.RateLimitProperties;
import com.yu.blog.common.service.RateLimitService;
import com.yu.blog.common.exception.UnauthorizedException;
import com.yu.blog.module.user.entity.SysUser;
import com.yu.blog.module.user.mapper.SysUserMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String ENABLED = "ENABLED";

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    public AuthTokenResponse login(LoginRequest request, String clientIp) {
        rateLimitService.checkLogin(clientIp, request.username(), rateLimitProperties);
        SysUser user = sysUserMapper.selectOne(
                Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, request.username())
        );

        if (user == null || !ENABLED.equals(user.getStatus())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        return issueTokens(user, jwtTokenService.generateRefreshToken(user));
    }

    public UserInfoResponse changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new UnauthorizedException("Unauthorized");
        }
        SysUser user = sysUserMapper.selectById(principal.id());
        if (user == null || !ENABLED.equals(user.getStatus())) {
            throw new UnauthorizedException("Unauthorized");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "New password must be different");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        sysUserMapper.updateById(user);
        return UserInfoResponse.from(user);
    }

    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        try {
            JwtClaims claims = jwtTokenService.parse(request.refreshToken());
            if (!claims.isRefreshToken()) {
                throw new UnauthorizedException("Refresh token required");
            }
            SysUser user = sysUserMapper.selectById(claims.userId());
            if (user == null || !ENABLED.equals(user.getStatus())) {
                throw new UnauthorizedException("Invalid refresh token");
            }
            return issueTokens(user, request.refreshToken());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("Invalid refresh token");
        }
    }

    public UserInfoResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new UnauthorizedException("Unauthorized");
        }
        SysUser user = sysUserMapper.selectById(principal.id());
        if (user == null || !ENABLED.equals(user.getStatus())) {
            throw new UnauthorizedException("Unauthorized");
        }
        return UserInfoResponse.from(user);
    }

    private AuthTokenResponse issueTokens(SysUser user, String refreshToken) {
        return new AuthTokenResponse(
                jwtTokenService.generateAccessToken(user),
                refreshToken,
                "Bearer",
                jwtTokenService.accessTokenExpireSeconds(),
                UserInfoResponse.from(user)
        );
    }
}
