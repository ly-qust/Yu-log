package com.yu.blog.module.article.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.cache.CacheKeys;
import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.article.vo.AdminArticleListVO;
import com.yu.blog.module.article.vo.ArticleDetailVO;
import com.yu.blog.module.article.vo.ArticleListVO;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCounterService {
    private static final long FAILURE_BACKOFF_MILLIS = 30_000;

    private final StringRedisTemplate redisTemplate;
    private final ArticleMapper articleMapper;
    private final AtomicLong redisUnavailableUntil = new AtomicLong(0);

    public long increaseViewCount(Long articleId, long databaseValue) {
        Long delta = increment(CacheKeys.articleViewDelta(articleId));
        if (delta == null) {
            articleMapper.update(null, Wrappers.lambdaUpdate(Article.class)
                    .eq(Article::getId, articleId)
                    .setSql("view_count = view_count + 1"));
            Article updated = articleMapper.selectById(articleId);
            return updated == null || updated.getViewCount() == null ? databaseValue + 1 : updated.getViewCount();
        }
        return databaseValue + delta;
    }

    public long increaseLikeCount(Long articleId, long databaseValue) {
        Long delta = increment(CacheKeys.articleLikeDelta(articleId));
        if (delta == null) {
            articleMapper.update(null, Wrappers.lambdaUpdate(Article.class)
                    .eq(Article::getId, articleId)
                    .setSql("like_count = like_count + 1"));
            Article updated = articleMapper.selectById(articleId);
            return updated == null || updated.getLikeCount() == null ? databaseValue + 1 : updated.getLikeCount();
        }
        return databaseValue + delta;
    }

    public long currentLikeCount(Long articleId, long databaseValue) {
        return databaseValue + delta(CacheKeys.articleLikeDelta(articleId));
    }

    public PageResult<ArticleListVO> mergePublicCounts(PageResult<ArticleListVO> page) {
        List<ArticleListVO> list = page.list().stream().map(this::mergeCounts).toList();
        return new PageResult<>(list, page.pageNum(), page.pageSize(), page.total(), page.totalPages(), page.hasNext(), page.hasPrevious());
    }

    public PageResult<AdminArticleListVO> mergeAdminCounts(PageResult<AdminArticleListVO> page) {
        List<AdminArticleListVO> list = page.list().stream().map(this::mergeCounts).toList();
        return new PageResult<>(list, page.pageNum(), page.pageSize(), page.total(), page.totalPages(), page.hasNext(), page.hasPrevious());
    }

    public ArticleDetailVO mergeCounts(ArticleDetailVO detail) {
        Long articleId = parseId(detail.id());
        if (articleId == null) {
            return detail;
        }
        return detail.withCounts(
                detail.viewCount() + delta(CacheKeys.articleViewDelta(articleId)),
                detail.likeCount() + delta(CacheKeys.articleLikeDelta(articleId))
        );
    }

    public ArticleListVO mergeCounts(ArticleListVO item) {
        Long articleId = parseId(item.id());
        if (articleId == null) {
            return item;
        }
        return item.withCounts(
                item.viewCount() + delta(CacheKeys.articleViewDelta(articleId)),
                item.likeCount() + delta(CacheKeys.articleLikeDelta(articleId))
        );
    }

    public AdminArticleListVO mergeCounts(AdminArticleListVO item) {
        Long articleId = parseId(item.id());
        if (articleId == null) {
            return item;
        }
        return item.withCounts(
                item.viewCount() + delta(CacheKeys.articleViewDelta(articleId)),
                item.likeCount() + delta(CacheKeys.articleLikeDelta(articleId))
        );
    }

    public void syncDeltasToDatabase() {
        syncPattern(CacheKeys.articleViewDeltaPattern(), "view_count");
        syncPattern(CacheKeys.articleLikeDeltaPattern(), "like_count");
    }

    private Long increment(String key) {
        if (isRedisTemporarilyUnavailable()) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception ex) {
            markRedisFailure();
            log.warn("Redis article counter increment failed, key={}", key, ex);
            return null;
        }
    }

    private long delta(String key) {
        if (isRedisTemporarilyUnavailable()) {
            return 0;
        }
        try {
            String value = redisTemplate.opsForValue().get(key);
            return parseLong(value);
        } catch (Exception ex) {
            markRedisFailure();
            log.warn("Redis article counter read failed, key={}", key, ex);
            return 0;
        }
    }

    private void syncPattern(String pattern, String column) {
        Set<String> keys = scanKeys(pattern);
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        for (String key : keys) {
            Long articleId = parseIdFromKey(key);
            if (articleId == null) {
                continue;
            }
            long count = delta(key);
            if (count <= 0) {
                deleteKey(key);
                continue;
            }
            try {
                articleMapper.update(null, Wrappers.lambdaUpdate(Article.class)
                        .eq(Article::getId, articleId)
                        .setSql(column + " = " + column + " + " + count));
                deleteKey(key);
                log.debug("Synced article counter, column={}, articleId={}, delta={}", column, articleId, count);
            } catch (Exception ex) {
                log.warn("Sync article counter failed, column={}, articleId={}, delta={}", column, articleId, count, ex);
            }
        }
    }

    private Set<String> scanKeys(String pattern) {
        if (isRedisTemporarilyUnavailable()) {
            return Set.of();
        }
        try {
            Set<String> keys = redisTemplate.execute((RedisCallback<Set<String>>) connection -> scan(connection, pattern));
            return keys == null ? Set.of() : keys;
        } catch (Exception ex) {
            markRedisFailure();
            log.warn("Redis article counter scan failed, pattern={}", pattern, ex);
            return Set.of();
        }
    }

    private Set<String> scan(RedisConnection connection, String pattern) {
        Set<String> keys = new HashSet<>();
        try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(500).build())) {
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
        }
        return keys;
    }

    private void deleteKey(String key) {
        if (isRedisTemporarilyUnavailable()) {
            return;
        }
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            markRedisFailure();
            log.warn("Redis article counter delete failed, key={}", key, ex);
        }
    }

    private boolean isRedisTemporarilyUnavailable() {
        return System.currentTimeMillis() < redisUnavailableUntil.get();
    }

    private void markRedisFailure() {
        redisUnavailableUntil.set(System.currentTimeMillis() + FAILURE_BACKOFF_MILLIS);
    }

    private Long parseIdFromKey(String key) {
        if (key == null) {
            return null;
        }
        int index = key.lastIndexOf(':');
        return index < 0 ? null : parseId(key.substring(index + 1));
    }

    private Long parseId(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private long parseLong(String value) {
        try {
            return value == null ? 0 : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
