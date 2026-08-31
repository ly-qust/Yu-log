package com.yu.blog.module.site.controller;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.site.service.SiteSettingService;
import com.yu.blog.module.site.vo.AboutVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/about")
@RequiredArgsConstructor
public class AboutController {
    private final SiteSettingService siteSettingService;

    @GetMapping
    public Result<AboutVO> about() {
        return Result.ok(siteSettingService.about());
    }
}
