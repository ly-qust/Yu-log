package com.yu.blog.module.site.vo;

import java.util.List;
import java.util.Map;

public record AboutVO(
        Map<String, Object> profile,
        List<String> skills,
        List<String> education,
        String learningPhilosophy
) {
}
