package com.yu.blog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = {
        "yu-log.rate-limit.login-ip-max-requests=2",
        "yu-log.rate-limit.login-username-max-requests=100",
        "yu-log.rate-limit.login-window-seconds=60"
})
@AutoConfigureMockMvc
class LoginRateLimitIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn429WithRetryAfterWhenLoginIpExceedsLimit() throws Exception {
        String clientIp = "198.51.100." + ThreadLocalRandom.current().nextInt(1, 255);
        RequestPostProcessor fromClientIp = request -> {
            request.setRemoteAddr(clientIp);
            return request;
        };
        String body = objectMapper.writeValueAsString(Map.of(
                "username", "yu_admin",
                "password", "incorrect-password"
        ));

        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .with(fromClientIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .with(fromClientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value(429));
    }
}
