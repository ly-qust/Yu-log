package com.yu.blog.module.site.vo;

import com.yu.blog.module.article.vo.ArticleListVO;
import com.yu.blog.module.note.vo.NoteVO;
import com.yu.blog.module.project.vo.ProjectVO;
import com.yu.blog.module.timeline.vo.TimelineEventVO;
import java.util.List;

public record HomeOverviewVO(
        HeroVO hero,
        StatsVO stats,
        List<ArticleListVO> latestArticles,
        List<ProjectVO> featuredProjects,
        List<NoteVO> latestNotes,
        List<TimelineEventVO> timelinePreview,
        List<String> currentlyLearning
) {
    public record HeroVO(
            String title,
            String subtitle,
            String description,
            String statusText
    ) {
    }

    public record StatsVO(
            long articleCount,
            long projectCount,
            long noteCount,
            long messageCount
    ) {
    }
}
