package com.yu.blog.module.user;

import com.yu.blog.auth.AuthService;
import com.yu.blog.auth.vo.UserInfoResponse;
import com.yu.blog.common.api.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;

    @GetMapping("/api/users/me")
    public Result<UserInfoResponse> me() {
        return Result.ok(authService.currentUser());
    }
}
