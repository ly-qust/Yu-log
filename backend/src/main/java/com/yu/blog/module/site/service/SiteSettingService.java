package com.yu.blog.module.site.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yu.blog.auth.AuthenticatedUser;
import com.yu.blog.common.cache.CacheKeys;
import com.yu.blog.common.cache.CacheProperties;
import com.yu.blog.common.cache.CacheService;
import com.yu.blog.common.util.JsonValueMapper;
import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.article.service.ArticleService;
import com.yu.blog.module.message.entity.Message;
import com.yu.blog.module.message.mapper.MessageMapper;
import com.yu.blog.module.note.service.NoteService;
import com.yu.blog.module.project.service.ProjectService;
import com.yu.blog.module.site.dto.SiteSettingBatchUpdateRequest;
import com.yu.blog.module.site.dto.SiteSettingUpdateRequest;
import com.yu.blog.module.site.entity.SiteSetting;
import com.yu.blog.module.site.mapper.SiteSettingMapper;
import com.yu.blog.module.site.vo.AboutVO;
import com.yu.blog.module.site.vo.HomeOverviewVO;
import com.yu.blog.module.site.vo.SiteSettingVO;
import com.yu.blog.module.timeline.service.TimelineService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SiteSettingService {
    private static final String PUBLISHED = "PUBLISHED";
    private static final String APPROVED = "APPROVED";

    private final SiteSettingMapper siteSettingMapper;
    private final ArticleMapper articleMapper;
    private final MessageMapper messageMapper;
    private final ArticleService articleService;
    private final ProjectService projectService;
    private final NoteService noteService;
    private final TimelineService timelineService;
    private final JsonValueMapper jsonValueMapper;
    private final CacheService cacheService;
    private final CacheProperties cacheProperties;

    public HomeOverviewVO homeOverview() {
        return cacheService.get(CacheKeys.homeOverview(), HomeOverviewVO.class)
                .orElseGet(() -> {
                    HomeOverviewVO overview = loadHomeOverview();
                    cacheService.put(CacheKeys.homeOverview(), overview, cacheProperties.homeTtl());
                    return overview;
                });
    }

    private HomeOverviewVO loadHomeOverview() {
        HomeOverviewVO.HeroVO hero = new HomeOverviewVO.HeroVO(
                setting("site.hero.title", setting("home.title", "你好，我是 Yu")),
                setting("site.hero.subtitle", setting("home.subtitle", "计算机科学与技术本科生")),
                setting("site.hero.description", "正在构建 Java 后端、Linux、数据库、Vue3 与 AI 应用能力。"),
                setting("site.hero.status_text", "Online & Learning")
        );
        HomeOverviewVO.StatsVO stats = new HomeOverviewVO.StatsVO(
                articleMapper.selectCount(Wrappers.lambdaQuery(Article.class).eq(Article::getStatus, PUBLISHED)),
                projectService.publicCount(),
                noteService.publicCount(),
                messageMapper.selectCount(Wrappers.lambdaQuery(Message.class).eq(Message::getStatus, APPROVED))
        );
        return new HomeOverviewVO(
                hero,
                stats,
                articleService.listPublicArticles(null, null, null, 1, 3, null).list(),
                projectService.latestFeatured(3),
                noteService.latestPublic(3),
                timelineService.latestPublic(4),
                jsonValueMapper.toStringListSetting(setting("site.currently_learning", setting("learning.current", "")))
        );
    }

    public AboutVO about() {
        return cacheService.get(CacheKeys.about(), AboutVO.class)
                .orElseGet(() -> {
                    AboutVO about = loadAbout();
                    cacheService.put(CacheKeys.about(), about, cacheProperties.aboutTtl());
                    return about;
                });
    }

    private AboutVO loadAbout() {
        Map<String, Object> profile = jsonValueMapper.toMap(setting("site.about.profile", ""));
        if (profile.isEmpty()) {
            profile = Map.of(
                    "nickname", "Yu",
                    "role", "计算机科学与技术本科生",
                    "avatar", "",
                    "description", setting("about.intro", "我正在记录课程设计、实习准备和工程实践。"),
                    "location", "",
                    "email", "",
                    "githubUrl", setting("site.social.github", setting("social.github", "")),
                    "careerDirection", List.of("Java 后端开发", "Linux 运维", "AI 应用开发")
            );
        }
        return new AboutVO(
                profile,
                jsonValueMapper.toStringListSetting(setting("site.about.skills", "")),
                jsonValueMapper.toStringListSetting(setting("site.about.education", "")),
                setting("site.about.philosophy", "把每一次项目实践沉淀为可复用的工程经验。")
        );
    }

    public List<SiteSettingVO> list(String group) {
        return siteSettingMapper.selectList(Wrappers.lambdaQuery(SiteSetting.class)
                        .eq(StringUtils.hasText(group), SiteSetting::getGroupName, group)
                        .orderByAsc(SiteSetting::getGroupName)
                        .orderByAsc(SiteSetting::getConfigKey))
                .stream()
                .map(SiteSettingVO::from)
                .toList();
    }

    @Transactional
    public SiteSettingVO update(String key, SiteSettingUpdateRequest request) {
        SiteSetting setting = siteSettingMapper.selectOne(Wrappers.lambdaQuery(SiteSetting.class)
                .eq(SiteSetting::getConfigKey, key));
        if (setting == null) {
            setting = new SiteSetting();
            setting.setConfigKey(key);
            setting.setConfigName(key);
        }
        applyUpdate(setting, request.settingValue(), request.settingType(), request.groupName(), request.description());
        saveOrUpdate(setting);
        SiteSettingVO result = SiteSettingVO.from(siteSettingMapper.selectOne(Wrappers.lambdaQuery(SiteSetting.class)
                .eq(SiteSetting::getConfigKey, key)));
        invalidateSiteCaches();
        return result;
    }

    @Transactional
    public List<SiteSettingVO> updateBatch(List<SiteSettingBatchUpdateRequest> requests) {
        for (SiteSettingBatchUpdateRequest request : requests) {
            SiteSetting setting = siteSettingMapper.selectOne(Wrappers.lambdaQuery(SiteSetting.class)
                    .eq(SiteSetting::getConfigKey, request.settingKey()));
            if (setting == null) {
                setting = new SiteSetting();
                setting.setConfigKey(request.settingKey());
                setting.setConfigName(request.settingKey());
            }
            applyUpdate(setting, request.settingValue(), request.settingType(), request.groupName(), request.description());
            saveOrUpdate(setting);
        }
        List<SiteSettingVO> result = list(null);
        invalidateSiteCaches();
        return result;
    }

    private void applyUpdate(SiteSetting setting, String value, String type, String group, String description) {
        setting.setConfigValue(value);
        setting.setConfigType(StringUtils.hasText(type) ? type : "TEXT");
        setting.setGroupName(StringUtils.hasText(group) ? group : defaultGroup(setting.getConfigKey()));
        setting.setDescription(description);
        setting.setUpdatedBy(currentUserId());
    }

    private void saveOrUpdate(SiteSetting setting) {
        if (setting.getId() == null) {
            siteSettingMapper.insert(setting);
        } else {
            siteSettingMapper.updateById(setting);
        }
    }

    private String setting(String key, String fallback) {
        SiteSetting setting = siteSettingMapper.selectOne(Wrappers.lambdaQuery(SiteSetting.class)
                .eq(SiteSetting::getConfigKey, key));
        if (setting == null || setting.getConfigValue() == null) {
            return fallback;
        }
        return setting.getConfigValue();
    }

    private String defaultGroup(String key) {
        if (key == null) {
            return "general";
        }
        if (key.contains(".hero") || key.contains("currently_learning") || key.startsWith("home.") || key.startsWith("learning.")) {
            return "home";
        }
        if (key.contains(".about") || key.startsWith("about.")) {
            return "about";
        }
        if (key.contains(".social") || key.startsWith("social.")) {
            return "social";
        }
        return "general";
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.id();
        }
        return 1L;
    }

    private void invalidateSiteCaches() {
        cacheService.evict(CacheKeys.homeOverview());
        cacheService.evict(CacheKeys.about());
    }
}
