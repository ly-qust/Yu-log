package com.yu.blog.module.project.vo;

import com.yu.blog.module.project.entity.Project;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectVO(
        String id,
        String name,
        String slug,
        String description,
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
    public static ProjectVO from(Project project, List<String> techStack) {
        return new ProjectVO(
                String.valueOf(project.getId()),
                project.getName(),
                project.getSlug(),
                project.getDescription(),
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
