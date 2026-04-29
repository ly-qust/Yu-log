package com.yu.blog.module.dev;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.category.mapper.CategoryMapper;
import com.yu.blog.module.message.mapper.MessageMapper;
import com.yu.blog.module.note.mapper.NoteMapper;
import com.yu.blog.module.project.mapper.ProjectMapper;
import com.yu.blog.module.tag.mapper.TagMapper;
import com.yu.blog.module.timeline.mapper.TimelineEventMapper;
import com.yu.blog.module.user.mapper.SysUserMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequiredArgsConstructor
public class DevDbCheckController {
    private final SysUserMapper sysUserMapper;
    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ProjectMapper projectMapper;
    private final NoteMapper noteMapper;
    private final TimelineEventMapper timelineEventMapper;
    private final MessageMapper messageMapper;

    @GetMapping("/api/dev/db-check")
    public Result<Map<String, Integer>> check() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("userCount", count(sysUserMapper.selectCount(null)));
        counts.put("articleCount", count(articleMapper.selectCount(null)));
        counts.put("categoryCount", count(categoryMapper.selectCount(null)));
        counts.put("tagCount", count(tagMapper.selectCount(null)));
        counts.put("projectCount", count(projectMapper.selectCount(null)));
        counts.put("noteCount", count(noteMapper.selectCount(null)));
        counts.put("timelineCount", count(timelineEventMapper.selectCount(null)));
        counts.put("messageCount", count(messageMapper.selectCount(null)));
        return Result.ok(counts);
    }

    private Integer count(Long value) {
        return Math.toIntExact(value);
    }
}
