package com.yu.blog.module.site.dto;

import jakarta.validation.constraints.NotNull;

public record SiteSettingUpdateRequest(
        @NotNull
        String settingValue,

        String settingType,

        String groupName,

        String description
) {
}
