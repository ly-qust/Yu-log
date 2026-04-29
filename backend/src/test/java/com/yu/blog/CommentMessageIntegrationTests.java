package com.yu.blog;

import static org.assertj.core.api.Assertions.assertThat;
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
class CommentMessageIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldModerateReplyRejectAndDeleteArticleComment() throws Exception {
        String token = loginToken();
        String articleId = firstPublicArticleId();
        String content = "stage5a comment " + System.nanoTime();

        mockMvc.perform(post("/api/articles/{articleId}/comments", articleId)
                        .header("X-Forwarded-For", uniqueIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentPayload(content))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertThat(publicCommentsContain(articleId, content)).isFalse();

        JsonNode pendingComment = firstAdminComment(token, articleId, content);
        String commentId = pendingComment.get("id").asText();
        assertThat(pendingComment.get("status").asText()).isEqualTo("PENDING");

        mockMvc.perform(put("/api/admin/comments/{id}/status", commentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "APPROVED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        assertThat(publicCommentsContain(articleId, content)).isTrue();

        mockMvc.perform(put("/api/admin/comments/{id}/reply", commentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("adminReply", "Thanks for the comment"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adminReply").value("Thanks for the comment"))
                .andExpect(jsonPath("$.data.repliedAt").isString());

        mockMvc.perform(put("/api/admin/comments/{id}/status", commentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "REJECTED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        assertThat(publicCommentsContain(articleId, content)).isFalse();

        mockMvc.perform(delete("/api/admin/comments/{id}", commentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void shouldRejectCommentForDraftArticle() throws Exception {
        String token = loginToken();
        String slug = "stage5a-draft-comment-target-" + System.nanoTime();

        String createResponse = mockMvc.perform(post("/api/admin/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articlePayload(slug, "DRAFT"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String draftArticleId = objectMapper.readTree(createResponse).at("/data/id").asText();

        mockMvc.perform(post("/api/articles/{articleId}/comments", draftArticleId)
                        .header("X-Forwarded-For", uniqueIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentPayload("comment for draft " + System.nanoTime()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(delete("/api/admin/articles/{id}", draftArticleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldModerateReplyAndDeleteMessage() throws Exception {
        String token = loginToken();
        String content = "stage5a message " + System.nanoTime();

        mockMvc.perform(post("/api/messages")
                        .header("X-Forwarded-For", uniqueIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messagePayload(content))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertThat(publicMessagesContain(content)).isFalse();

        JsonNode pendingMessage = firstAdminMessage(token, content);
        String messageId = pendingMessage.get("id").asText();
        assertThat(pendingMessage.get("status").asText()).isEqualTo("PENDING");

        mockMvc.perform(put("/api/admin/messages/{id}/status", messageId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "APPROVED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        assertThat(publicMessagesContain(content)).isTrue();

        mockMvc.perform(put("/api/admin/messages/{id}/reply", messageId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("adminReply", "Thanks for the message"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adminReply").value("Thanks for the message"))
                .andExpect(jsonPath("$.data.repliedAt").isString());

        mockMvc.perform(delete("/api/admin/messages/{id}", messageId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void shouldProtectAdminCommentAndMessageApis() throws Exception {
        mockMvc.perform(get("/api/admin/comments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/admin/messages"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        String token = loginToken();
        mockMvc.perform(get("/api/admin/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());

        mockMvc.perform(get("/api/admin/messages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    void shouldKeepExistingHealthDashboardAndArticleApisWorking() throws Exception {
        String token = loginToken();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.articleCount").value(greaterThanOrEqualTo(3)));

        mockMvc.perform(get("/api/articles")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());
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

    private JsonNode firstAdminComment(String token, String articleId, String keyword) throws Exception {
        String response = mockMvc.perform(get("/api/admin/comments")
                        .header("Authorization", "Bearer " + token)
                        .param("articleId", articleId)
                        .param("keyword", keyword)
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/list/0");
    }

    private JsonNode firstAdminMessage(String token, String keyword) throws Exception {
        String response = mockMvc.perform(get("/api/admin/messages")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", keyword)
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/list/0");
    }

    private boolean publicCommentsContain(String articleId, String content) throws Exception {
        String response = mockMvc.perform(get("/api/articles/{articleId}/comments", articleId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        for (JsonNode item : objectMapper.readTree(response).at("/data")) {
            if (content.equals(item.get("content").asText())) {
                return true;
            }
        }
        return false;
    }

    private boolean publicMessagesContain(String content) throws Exception {
        String response = mockMvc.perform(get("/api/messages").param("page", "1").param("size", "50"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        for (JsonNode item : objectMapper.readTree(response).at("/data/list")) {
            if (content.equals(item.get("content").asText())) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> commentPayload(String content) {
        return Map.of(
                "nickname", "Visitor",
                "email", "visitor@example.com",
                "content", content
        );
    }

    private Map<String, Object> messagePayload(String content) {
        return Map.of(
                "nickname", "Guest",
                "email", "guest@example.com",
                "content", content
        );
    }

    private Map<String, Object> articlePayload(String slug, String status) {
        return Map.of(
                "title", "Stage5A draft article",
                "slug", slug,
                "summary", "Draft article for comment validation",
                "content", "## Stage5A\n\nDraft article should not accept public comments.",
                "coverImage", "",
                "categoryId", 100,
                "tagIds", List.of(200),
                "status", status,
                "isTop", false,
                "readingTime", 2
        );
    }

    private String uniqueIp() {
        long value = Math.abs(System.nanoTime());
        return "10." + (value % 200 + 1) + "." + (value % 250 + 1) + "." + (value % 240 + 1);
    }
}
