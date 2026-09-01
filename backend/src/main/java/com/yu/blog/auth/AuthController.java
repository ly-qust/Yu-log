package com.yu.blog.auth;

import com.yu.blog.auth.dto.LoginRequest;
import com.yu.blog.auth.dto.RefreshTokenRequest;
import com.yu.blog.auth.vo.AuthTokenResponse;
import com.yu.blog.auth.vo.UserInfoResponse;
import com.yu.blog.auth.dto.ChangePasswordRequest;
import com.yu.blog.common.api.Result;
import com.yu.blog.module.log.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final com.yu.blog.config.ClientIpResolver clientIpResolver;

    @PostMapping("/login")
    public Result<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        AuthTokenResponse response = authService.login(request, clientIpResolver.resolve(servletRequest));
        servletRequest.setAttribute(OperationLogService.OPERATOR_USER_ID_ATTRIBUTE, Long.valueOf(response.user().id()));
        return Result.ok(response);
    }

    @PostMapping("/refresh")
    public Result<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

    @PostMapping("/change-password")
    public Result<UserInfoResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return Result.ok(authService.changePassword(request));
    }
}
