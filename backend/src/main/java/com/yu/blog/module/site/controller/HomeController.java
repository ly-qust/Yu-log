package com.yu.blog.module.site.controller;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.site.service.SiteSettingService;
import com.yu.blog.module.site.vo.HomeOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {
    private final SiteSettingService siteSettingService;

    @GetMapping("/overview")
    public Result<HomeOverviewVO> overview() {
        return Result.ok(siteSettingService.homeOverview());
    }
}
