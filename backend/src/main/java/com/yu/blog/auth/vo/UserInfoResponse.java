package com.yu.blog.auth.vo;

import com.yu.blog.module.user.entity.SysUser;

public record UserInfoResponse(
        String id,
        String username,
        String nickname,
        String roleCode
) {
    public static UserInfoResponse from(SysUser user) {
        return new UserInfoResponse(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getNickname(),
                user.getRoleCode()
        );
    }
}
