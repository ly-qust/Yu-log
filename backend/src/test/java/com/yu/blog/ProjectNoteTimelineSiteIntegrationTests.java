package com.yu.blog;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectNoteTimelineSiteIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldExposePublicHomeProjectsNotesTimelineAndAbout() throws Exception {
        mockMvc.perform(get("/api/home/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.hero.title").isString())
                .andExpect(jsonPath("$.data.stats.articleCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.latestArticles").isArray())
                .andExpect(jsonPath("$.data.featuredProjects").isArray())
                .andExpect(jsonPath("$.data.latestNotes").isArray())
                .andExpect(jsonPath("$.data.timelinePreview").isArray());

        String projectId = firstId("/api/projects");
        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(projectId))
                .andExpect(jsonPath("$.data.detailContent").exists());

        String noteId = firstId("/api/notes");
        mockMvc.perform(get("/api/notes/{id}", noteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(noteId))
                .andExpect(jsonPath("$.data.content").isString());

        mockMvc.perform(get("/api/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.list[0].id").isString());

        mockMvc.perform(get("/api/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.nickname").isString())
                .andExpect(jsonPath("$.data.skills").isArray());
    }

    @Test
    void shouldProtectAdminProjectsAndCrudProject() throws Exception {
        mockMvc.perform(get("/api/admin/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        String token = loginToken();
        String slug = "stage6a-project-" + System.nanoTime();

        String createResponse = mockMvc.perform(post("/api/admin/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectPayload(slug, "COMPLETED", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isString())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String projectId = objectMapper.readTree(createResponse).at("/data/id").asText();

        mockMvc.perform(put("/api/admin/projects/{id}", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectPayload(slug, "DEVELOPING", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(projectId))
                .andExpect(jsonPath("$.data.status").value("DEVELOPING"));

        mockMvc.perform(delete("/api/admin/projects/{id}", projectId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void shouldCrudNotesTimelineAndUpdateSiteSettings() throws Exception {
        String token = loginToken();
        String unique = String.valueOf(System.nanoTime());

        String noteSlug = "stage6a-note-" + unique;
        String noteResponse = mockMvc.perform(post("/api/admin/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notePayload(noteSlug, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value(noteSlug))
                .andExpect(jsonPath("$.data.isPublic").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String noteId = objectMapper.readTree(noteResponse).at("/data/id").asText();

        mockMvc.perform(put("/api/admin/notes/{id}", noteId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notePayload(noteSlug, false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isPublic").value(false));

        mockMvc.perform(delete("/api/admin/notes/{id}", noteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String timelineResponse = mockMvc.perform(post("/api/admin/timeline")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timelinePayload("Stage6A 时间线 " + unique, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Stage6A 时间线 " + unique))
                .andExpect(jsonPath("$.data.visible").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String timelineId = objectMapper.readTree(timelineResponse).at("/data/id").asText();

        mockMvc.perform(put("/api/admin/timeline/{id}", timelineId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timelinePayload("Stage6A 时间线更新 " + unique, false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.visible").value(false));

        mockMvc.perform(delete("/api/admin/timeline/{id}", timelineId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String philosophy = "Stage6A 学习理念 " + unique;
        mockMvc.perform(put("/api/admin/site-settings/{key}", "site.about.philosophy")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "settingValue", philosophy,
                                "settingType", "TEXT",
                                "groupName", "about",
                                "description", "测试关于我学习理念"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.settingValue").value(philosophy));

        mockMvc.perform(get("/api/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.learningPhilosophy").value(philosophy));

        String heroTitle = "Stage6A 首页标题 " + unique;
        mockMvc.perform(put("/api/admin/site-settings/{key}", "site.hero.title")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "settingValue", heroTitle,
                                "settingType", "TEXT",
                                "groupName", "home",
                                "description", "测试首页标题"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/home/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hero.title").value(heroTitle));
    }

    @Test
    void shouldKeepExistingCoreApisWorking() throws Exception {
        String token = loginToken();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.articleCount").value(greaterThanOrEqualTo(1)));
    }

    private String firstId(String url) throws Exception {
        String response = mockMvc.perform(get(url).param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/list/0/id").asText();
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

    private Map<String, Object> projectPayload(String slug, String status, boolean visible) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Stage6A 项目");
        payload.put("slug", slug);
        payload.put("description", "用于验证项目管理接口");
        payload.put("detailContent", "## Stage6A 项目\n\n项目详情内容。");
        payload.put("coverImage", "");
        payload.put("techStack", List.of("Java", "Vue3", "MySQL"));
        payload.put("status", status);
        payload.put("githubUrl", "https://github.com/example/stage6a");
        payload.put("demoUrl", "https://example.com");
        payload.put("sortOrder", 88);
        payload.put("visible", visible);
        return payload;
    }

    private Map<String, Object> notePayload(String slug, boolean isPublic) {
        return Map.of(
                "title", "Stage6A 笔记",
                "slug", slug,
                "summary", "用于验证笔记管理接口",
                "content", "## Stage6A 笔记\n\n笔记正文内容。",
                "topic", "BACKEND",
                "tags", List.of("Java", "Spring Boot"),
                "isPublic", isPublic,
                "sortOrder", 66
        );
    }

    private Map<String, Object> timelinePayload(String title, boolean visible) {
        return Map.of(
                "title", title,
                "description", "用于验证时间线管理接口",
                "eventDate", "2026-04-29",
                "type", "MILESTONE",
                "tags", List.of("Stage6A", "CMS"),
                "sortOrder", 55,
                "visible", visible
        );
    }
}
