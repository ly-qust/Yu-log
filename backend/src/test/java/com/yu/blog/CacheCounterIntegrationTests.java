package com.yu.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.blog.common.cache.CacheKeys;
import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.article.service.ArticleCounterService;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "yu-log.article-counter.initial-delay-ms=600000")
@AutoConfigureMockMvc
@Transactional
class CacheCounterIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleCounterService articleCounterService;

    @BeforeEach
    void setUp() {
        cleanRedis();
    }

    @AfterEach
    void tearDown() {
        cleanRedis();
    }

    @Test
    void shouldCacheHomeOverviewAndInvalidateAfterSiteSettingUpdate() throws Exception {
        mockMvc.perform(get("/api/home/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        assertThat(redisTemplate.hasKey(CacheKeys.homeOverview())).isTrue();

        String token = loginToken();
        String title = "Stage7B Home " + System.nanoTime();
        mockMvc.perform(put("/api/admin/site-settings/{key}", "site.hero.title")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "settingValue", title,
                                "settingType", "TEXT",
                                "groupName", "home",
                                "description", "Stage7B cache test"
                        ))))
                .andExpect(status().isOk());
        assertThat(redisTemplate.hasKey(CacheKeys.homeOverview())).isFalse();

        mockMvc.perform(get("/api/home/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hero.title").value(title));
    }

    @Test
    void shouldCacheCategoriesAndInvalidateAfterCategoryCreate() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        assertThat(redisTemplate.hasKey(CacheKeys.categoryList())).isTrue();

        String token = loginToken();
        String unique = String.valueOf(System.nanoTime());
        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Stage7B Category " + unique,
                                "slug", "stage7b-category-" + unique,
                                "description", "Cache invalidation test",
                                "sortOrder", 199,
                                "status", "ENABLED"
                        ))))
                .andExpect(status().isOk());

        assertThat(redisTemplate.hasKey(CacheKeys.categoryList())).isFalse();
    }

    @Test
    void shouldStoreArticleViewAndLikeDeltaInRedisThenSyncToDatabase() throws Exception {
        Article article = firstPublishedArticle();
        long initialViewCount = safeLong(article.getViewCount());
        long initialLikeCount = safeLong(article.getLikeCount());

        mockMvc.perform(get("/api/articles/{id}", article.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(initialViewCount + 1));
        assertThat(redisTemplate.opsForValue().get(CacheKeys.articleViewDelta(article.getId()))).isEqualTo("1");

        mockMvc.perform(post("/api/articles/{id}/like", article.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(initialLikeCount + 1));
        assertThat(redisTemplate.opsForValue().get(CacheKeys.articleLikeDelta(article.getId()))).isEqualTo("1");

        articleCounterService.syncDeltasToDatabase();

        Article updated = articleMapper.selectById(article.getId());
        assertThat(safeLong(updated.getViewCount())).isEqualTo(initialViewCount + 1);
        assertThat(safeLong(updated.getLikeCount())).isEqualTo(initialLikeCount + 1);
        assertThat(redisTemplate.hasKey(CacheKeys.articleViewDelta(article.getId()))).isFalse();
        assertThat(redisTemplate.hasKey(CacheKeys.articleLikeDelta(article.getId()))).isFalse();
    }

    private Article firstPublishedArticle() {
        return articleMapper.selectList(Wrappers.lambdaQuery(Article.class)
                        .eq(Article::getStatus, "PUBLISHED")
                        .orderByAsc(Article::getId)
                        .last("LIMIT 1"))
                .getFirst();
    }

    private String loginToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "yu_admin",
                                "password", "Yu@123456"
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/accessToken").asText();
    }

    private long safeLong(Long value) {
        return value == null ? 0 : value;
    }

    private void cleanRedis() {
        Set<String> keys = redisTemplate.keys("yu-log:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
