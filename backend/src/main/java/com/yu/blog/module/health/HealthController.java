package com.yu.blog.module.health;

import com.yu.blog.common.api.Result;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final HealthEndpoint healthEndpoint;

    public HealthController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Result<Map<String, String>>> health() {
        HealthComponent component = healthEndpoint.health();
        boolean up = Status.UP.equals(component.getStatus());
        Result<Map<String, String>> result = Result.ok(Map.of(
                "status", component.getStatus().getCode(),
                "service", "yu-log-backend",
                "time", OffsetDateTime.now().toString()
        ));
        return ResponseEntity.status(up ? 200 : 503).body(result);
    }
}
