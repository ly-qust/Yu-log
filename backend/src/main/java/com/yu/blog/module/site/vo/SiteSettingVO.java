package com.yu.blog.module.site.vo;

import com.yu.blog.module.site.entity.SiteSetting;
import java.time.LocalDateTime;

public record SiteSettingVO(
        String id,
        String settingKey,
        String settingValue,
        String settingType,
        String groupName,
        String description,
        LocalDateTime updatedAt
) {
    public static SiteSettingVO from(SiteSetting setting) {
        return new SiteSettingVO(
                String.valueOf(setting.getId()),
                setting.getConfigKey(),
                setting.getConfigValue(),
                setting.getConfigType(),
                setting.getGroupName(),
                setting.getDescription(),
                setting.getUpdatedAt()
        );
    }
}
