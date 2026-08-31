package com.yu.blog.common.cache;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yu-log.cache")
public class CacheProperties {
    private boolean enabled = true;
    private long homeTtlSeconds = 300;
    private long categoryTtlSeconds = 1800;
    private long tagTtlSeconds = 1800;
    private long articleTtlSeconds = 300;
    private long projectTtlSeconds = 600;
    private long noteTtlSeconds = 600;
    private long timelineTtlSeconds = 600;
    private long aboutTtlSeconds = 1800;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getHomeTtlSeconds() {
        return homeTtlSeconds;
    }

    public void setHomeTtlSeconds(long homeTtlSeconds) {
        this.homeTtlSeconds = homeTtlSeconds;
    }

    public long getCategoryTtlSeconds() {
        return categoryTtlSeconds;
    }

    public void setCategoryTtlSeconds(long categoryTtlSeconds) {
        this.categoryTtlSeconds = categoryTtlSeconds;
    }

    public long getTagTtlSeconds() {
        return tagTtlSeconds;
    }

    public void setTagTtlSeconds(long tagTtlSeconds) {
        this.tagTtlSeconds = tagTtlSeconds;
    }

    public long getArticleTtlSeconds() {
        return articleTtlSeconds;
    }

    public void setArticleTtlSeconds(long articleTtlSeconds) {
        this.articleTtlSeconds = articleTtlSeconds;
    }

    public long getProjectTtlSeconds() {
        return projectTtlSeconds;
    }

    public void setProjectTtlSeconds(long projectTtlSeconds) {
        this.projectTtlSeconds = projectTtlSeconds;
    }

    public long getNoteTtlSeconds() {
        return noteTtlSeconds;
    }

    public void setNoteTtlSeconds(long noteTtlSeconds) {
        this.noteTtlSeconds = noteTtlSeconds;
    }

    public long getTimelineTtlSeconds() {
        return timelineTtlSeconds;
    }

    public void setTimelineTtlSeconds(long timelineTtlSeconds) {
        this.timelineTtlSeconds = timelineTtlSeconds;
    }

    public long getAboutTtlSeconds() {
        return aboutTtlSeconds;
    }

    public void setAboutTtlSeconds(long aboutTtlSeconds) {
        this.aboutTtlSeconds = aboutTtlSeconds;
    }

    public Duration homeTtl() {
        return Duration.ofSeconds(homeTtlSeconds);
    }

    public Duration categoryTtl() {
        return Duration.ofSeconds(categoryTtlSeconds);
    }

    public Duration tagTtl() {
        return Duration.ofSeconds(tagTtlSeconds);
    }

    public Duration articleTtl() {
        return Duration.ofSeconds(articleTtlSeconds);
    }

    public Duration projectTtl() {
        return Duration.ofSeconds(projectTtlSeconds);
    }

    public Duration noteTtl() {
        return Duration.ofSeconds(noteTtlSeconds);
    }

    public Duration timelineTtl() {
        return Duration.ofSeconds(timelineTtlSeconds);
    }

    public Duration aboutTtl() {
        return Duration.ofSeconds(aboutTtlSeconds);
    }
}
