package com.yu.blog.module.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SiteSettingBatchUpdateRequest(
        @NotBlank
        String settingKey,

        @NotNull
        String settingValue,

        String settingType,

        String groupName,

        String description
) {
}
