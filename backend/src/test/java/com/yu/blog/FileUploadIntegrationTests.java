package com.yu.blog;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FileUploadIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRequireAdminAndUploadImageThenServePublicUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.png",
                "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}
        );

        mockMvc.perform(multipart("/api/admin/files/upload")
                        .file(file)
                        .param("bizType", "article-cover"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        String response = mockMvc.perform(multipart("/api/admin/files/upload")
                        .file(file)
                        .param("bizType", "article-cover")
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.url").value(startsWith("/uploads/")))
                .andExpect(jsonPath("$.data.filename").value(endsWith(".png")))
                .andExpect(jsonPath("$.data.originalFilename").value("cover.png"))
                .andExpect(jsonPath("$.data.contentType").value("image/png"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String url = objectMapper.readTree(response).at("/data/url").asText();
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}));
    }

    @Test
    void shouldRejectIllegalEmptyAndOversizedFiles() throws Exception {
        String token = loginToken();

        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "readme.txt",
                "text/plain",
                "hello".getBytes()
        );
                mockMvc.perform(multipart("/api/admin/files/upload")
                        .file(textFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("仅支持 jpg、png、webp、gif 图片"));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );
        mockMvc.perform(multipart("/api/admin/files/upload")
                        .file(emptyFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("上传文件不能为空"));

        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                new byte[5 * 1024 * 1024 + 1]
        );
        mockMvc.perform(multipart("/api/admin/files/upload")
                        .file(oversizedFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.message").value("文件大小不能超过 5MB"));
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
}
