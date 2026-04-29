package com.yu.blog.module.health;

import com.yu.blog.common.api.Result;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of(
                "status", "UP",
                "service", "yu-log-backend",
                "time", OffsetDateTime.now().toString()
        ));
    }
}
