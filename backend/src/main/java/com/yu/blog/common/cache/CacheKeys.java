package com.yu.blog.common.cache;

import java.util.Arrays;

public final class CacheKeys {
    private static final String PREFIX = "yu-log";

    private CacheKeys() {
    }

    public static String homeOverview() {
        return PREFIX + ":home:overview";
    }

    public static String about() {
        return PREFIX + ":about";
    }

    public static String categoryList() {
        return PREFIX + ":category:list";
    }

    public static String tagList() {
        return PREFIX + ":tag:list";
    }

    public static String articleList(String keyword, Long categoryId, Long tagId, long page, long size, String sort) {
        return PREFIX + ":article:list:" + hash(keyword, categoryId, tagId, page, size, sort);
    }

    public static String articleListPattern() {
        return PREFIX + ":article:list:*";
    }

    public static String articleViewDelta(Long articleId) {
        return PREFIX + ":article:view:delta:" + articleId;
    }

    public static String articleViewDeltaPattern() {
        return PREFIX + ":article:view:delta:*";
    }

    public static String articleLikeDelta(Long articleId) {
        return PREFIX + ":article:like:delta:" + articleId;
    }

    public static String articleLikeDeltaPattern() {
        return PREFIX + ":article:like:delta:*";
    }

    public static String projectList(String keyword, String techStack, String status, long page, long size) {
        return PREFIX + ":project:list:" + hash(keyword, techStack, status, page, size);
    }

    public static String projectListPattern() {
        return PREFIX + ":project:list:*";
    }

    public static String noteList(String keyword, String topic, long page, long size) {
        return PREFIX + ":note:list:" + hash(keyword, topic, page, size);
    }

    public static String noteListPattern() {
        return PREFIX + ":note:list:*";
    }

    public static String timelineList(String type, long page, long size) {
        return PREFIX + ":timeline:list:" + hash(type, page, size);
    }

    public static String timelineListPattern() {
        return PREFIX + ":timeline:list:*";
    }

    private static String hash(Object... parts) {
        return Integer.toUnsignedString(Arrays.deepHashCode(parts), 36);
    }
}
