package com.yu.blog.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yu.blog.module.user.entity.SysUser;
import com.yu.blog.module.user.mapper.SysUserMapper;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {
    private static final String ADMIN = "ADMIN";
    private static final String ENABLED = "ENABLED";

    private final AdminProperties properties;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.username()) && !StringUtils.hasText(properties.password())) {
            return;
        }
        if (!StringUtils.hasText(properties.username()) || !StringUtils.hasText(properties.password())) {
            throw new IllegalStateException("Admin bootstrap requires both username and password");
        }
        if (properties.password().length() < 12 || properties.password().length() > 72) {
            throw new IllegalStateException("Admin bootstrap password must be between 12 and 72 characters");
        }

        SysUser existing = sysUserMapper.selectOne(Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getRoleCode, ADMIN)
                .eq(SysUser::getStatus, ENABLED)
                .last("LIMIT 1"));
        if (existing != null) {
            return;
        }

        SysUser admin = new SysUser();
        admin.setUsername(properties.username().trim());
        admin.setNickname(properties.username().trim());
        admin.setPasswordHash(passwordEncoder.encode(properties.password()));
        admin.setRoleCode(ADMIN);
        admin.setStatus(ENABLED);
        admin.setMustChangePassword(false);
        sysUserMapper.insert(admin);
    }
}
