package com.yu.blog.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yu-log.trusted-proxy")
public class TrustedProxyProperties {
    private List<String> cidrs = new ArrayList<>(List.of(
            "127.0.0.1/32",
            "::1/128",
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16"
    ));

    public List<String> getCidrs() {
        return cidrs;
    }

    public void setCidrs(List<String> cidrs) {
        if (cidrs == null) {
            this.cidrs = new ArrayList<>();
            return;
        }
        this.cidrs = cidrs.stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
