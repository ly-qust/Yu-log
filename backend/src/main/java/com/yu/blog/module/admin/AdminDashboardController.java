package com.yu.blog.module.admin;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.message.mapper.MessageMapper;
import com.yu.blog.module.note.mapper.NoteMapper;
import com.yu.blog.module.project.mapper.ProjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminDashboardController {
    private final ArticleMapper articleMapper;
    private final MessageMapper messageMapper;
    private final ProjectMapper projectMapper;
    private final NoteMapper noteMapper;

    @GetMapping("/api/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Integer>> dashboard() {
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("articleCount", count(articleMapper.selectCount(null)));
        data.put("messageCount", count(messageMapper.selectCount(null)));
        data.put("projectCount", count(projectMapper.selectCount(null)));
        data.put("noteCount", count(noteMapper.selectCount(null)));
        return Result.ok(data);
    }

    private Integer count(Long value) {
        return Math.toIntExact(value);
    }
}
