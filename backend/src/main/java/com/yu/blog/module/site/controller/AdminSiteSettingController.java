package com.yu.blog.module.site.controller;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.site.dto.SiteSettingBatchUpdateRequest;
import com.yu.blog.module.site.dto.SiteSettingUpdateRequest;
import com.yu.blog.module.site.service.SiteSettingService;
import com.yu.blog.module.site.vo.SiteSettingVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/site-settings")
@RequiredArgsConstructor
public class AdminSiteSettingController {
    private final SiteSettingService siteSettingService;

    @GetMapping
    public Result<List<SiteSettingVO>> list(@RequestParam(required = false) String group) {
        return Result.ok(siteSettingService.list(group));
    }

    @PutMapping("/{key}")
    public Result<SiteSettingVO> update(
            @PathVariable String key,
            @Valid @RequestBody SiteSettingUpdateRequest request
    ) {
        return Result.ok(siteSettingService.update(key, request));
    }

    @PutMapping("/batch")
    public Result<List<SiteSettingVO>> updateBatch(
            @Valid @RequestBody List<SiteSettingBatchUpdateRequest> requests
    ) {
        return Result.ok(siteSettingService.updateBatch(requests));
    }
}
