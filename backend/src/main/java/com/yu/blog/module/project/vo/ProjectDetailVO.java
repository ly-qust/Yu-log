package com.yu.blog.module.project.vo;

import com.yu.blog.module.project.entity.Project;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectDetailVO(
        String id,
        String name,
        String slug,
        String description,
        String detailContent,
        String coverImage,
        List<String> techStack,
        String status,
        String githubUrl,
        String demoUrl,
        Integer sortOrder,
        Boolean visible,
        Boolean featured,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectDetailVO from(Project project, List<String> techStack) {
        return new ProjectDetailVO(
                String.valueOf(project.getId()),
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getDetailContent(),
                project.getCoverImageUrl(),
                techStack == null ? List.of() : techStack,
                project.getStatus(),
                project.getRepoUrl(),
                project.getDemoUrl(),
                project.getSortOrder(),
                project.getVisible(),
                project.getIsFeatured(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
