package com.yu.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleCategoryTagIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProtectAdminArticlesAndAllowAdminToken() throws Exception {
        mockMvc.perform(get("/api/admin/articles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/admin/articles")
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    void shouldListPublishedArticlesAndReadDetail() throws Exception {
        String listResponse = mockMvc.perform(get("/api/articles")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.list[0].id").isString())
                .andExpect(jsonPath("$.data.list[0].tags").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = objectMapper.readTree(listResponse).at("/data/list/0/id").asText();
        mockMvc.perform(get("/api/articles/{id}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(articleId))
                .andExpect(jsonPath("$.data.content").isString())
                .andExpect(jsonPath("$.data.tags").isArray());
    }

    @Test
    void shouldIncreaseViewCountAndLikeCount() throws Exception {
        String articleId = firstPublicArticleId();

        long firstViewCount = getPublicDetail(articleId).at("/data/viewCount").asLong();
        long secondViewCount = getPublicDetail(articleId).at("/data/viewCount").asLong();
        assertThat(secondViewCount).isGreaterThan(firstViewCount);

        long firstLikeCount = likeArticle(articleId);
        long secondLikeCount = likeArticle(articleId);
        assertThat(secondLikeCount).isEqualTo(firstLikeCount + 1);
    }

    @Test
    void shouldCreateUpdatePublishTopAndDeleteArticle() throws Exception {
        String token = loginToken();
        String slug = "stage4a-test-article-" + System.currentTimeMillis();

        String createResponse = mockMvc.perform(post("/api/admin/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articlePayload(slug, "DRAFT", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isString())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String articleId = objectMapper.readTree(createResponse).at("/data/id").asText();

        mockMvc.perform(put("/api/admin/articles/{id}", articleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articlePayload(slug, "HIDDEN", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(articleId))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"));

        mockMvc.perform(put("/api/admin/articles/{id}/status", articleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PUBLISHED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.publishedAt").isString());

        mockMvc.perform(put("/api/admin/articles/{id}/top", articleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("isTop", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isTop").value(true));

        mockMvc.perform(delete("/api/admin/articles/{id}", articleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void shouldCreateUpdateDeleteCategoryAndBlockUsedCategoryDelete() throws Exception {
        String token = loginToken();
        String slug = "stage4a-category-" + System.currentTimeMillis();

        String createResponse = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Stage4A 分类",
                                "slug", slug,
                                "description", "集成测试分类",
                                "sortOrder", 99,
                                "status", "ENABLED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String categoryId = objectMapper.readTree(createResponse).at("/data/id").asText();

        mockMvc.perform(put("/api/admin/categories/{id}", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Stage4A 分类更新",
                                "slug", slug,
                                "description", "集成测试分类更新",
                                "sortOrder", 100,
                                "status", "ENABLED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Stage4A 分类更新"));

        mockMvc.perform(delete("/api/admin/categories/{id}", categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/admin/categories/{id}", 100)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldCreateUpdateDeleteTagAndBlockUsedTagDelete() throws Exception {
        String token = loginToken();
        String unique = String.valueOf(System.currentTimeMillis());
        String slug = "stage4a-tag-" + unique;

        String createResponse = mockMvc.perform(post("/api/admin/tags")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Stage4A 标签 " + unique,
                                "slug", slug,
                                "color", "#67e8f9",
                                "description", "集成测试标签",
                                "status", "ENABLED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String tagId = objectMapper.readTree(createResponse).at("/data/id").asText();

        mockMvc.perform(put("/api/admin/tags/{id}", tagId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Stage4A 标签更新 " + unique,
                                "slug", slug,
                                "color", "#a7f3d0",
                                "description", "集成测试标签更新",
                                "status", "ENABLED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Stage4A 标签更新 " + unique));

        mockMvc.perform(delete("/api/admin/tags/{id}", tagId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/admin/tags/{id}", 200)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldExposePublicCategoriesAndTags() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data[0].id").isString());

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data[0].id").isString());
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

    private String firstPublicArticleId() throws Exception {
        String response = mockMvc.perform(get("/api/articles").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/list/0/id").asText();
    }

    private JsonNode getPublicDetail(String articleId) throws Exception {
        String response = mockMvc.perform(get("/api/articles/{id}", articleId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private long likeArticle(String articleId) throws Exception {
        String response = mockMvc.perform(post("/api/articles/{id}/like", articleId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/likeCount").asLong();
    }

    private Map<String, Object> articlePayload(String slug, String status, boolean isTop) {
        return Map.of(
                "title", "Stage4A 后端接口测试文章",
                "slug", slug,
                "summary", "用于验证文章、分类、标签主链路。",
                "content", "## Stage4A\n\n这是一篇由集成测试创建的 Markdown 测试文章。",
                "coverImage", "",
                "categoryId", 100,
                "tagIds", List.of(200, 201),
                "status", status,
                "isTop", isTop,
                "readingTime", 3
        );
    }
}
