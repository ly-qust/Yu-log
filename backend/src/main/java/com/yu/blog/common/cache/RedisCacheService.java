package com.yu.blog.common.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.blog.common.api.PageResult;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {
    private static final long FAILURE_BACKOFF_MILLIS = 30_000;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheProperties cacheProperties;
    private final AtomicLong redisUnavailableUntil = new AtomicLong(0);

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return getValue(key, objectMapper.getTypeFactory().constructType(type));
    }

    @Override
    public <T> Optional<List<T>> getList(String key, Class<T> itemType) {
        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, itemType);
        return getValue(key, javaType);
    }

    @Override
    public <T> Optional<PageResult<T>> getPage(String key, Class<T> itemType) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(PageResult.class, itemType);
        return getValue(key, javaType);
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        if (!cacheProperties.isEnabled() || isRedisTemporarilyUnavailable()
                || value == null || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ex) {
            markRedisFailure();
            log.warn("Redis cache put failed, key={}", key, ex);
        }
    }

    @Override
    public void evict(String key) {
        if (!cacheProperties.isEnabled() || isRedisTemporarilyUnavailable() || !StringUtils.hasText(key)) {
            return;
        }
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            markRedisFailure();
            log.warn("Redis cache evict failed, key={}", key, ex);
        }
    }

    @Override
    public void evictByPattern(String pattern) {
        if (!cacheProperties.isEnabled() || isRedisTemporarilyUnavailable() || !StringUtils.hasText(pattern)) {
            return;
        }
        try {
            Set<String> keys = scanKeys(pattern);
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ex) {
            markRedisFailure();
            log.warn("Redis cache evict pattern failed, pattern={}", pattern, ex);
        }
    }

    private <T> Optional<T> getValue(String key, JavaType javaType) {
        if (!cacheProperties.isEnabled() || isRedisTemporarilyUnavailable() || !StringUtils.hasText(key)) {
            return Optional.empty();
        }
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, javaType));
        } catch (Exception ex) {
            markRedisFailure();
            log.warn("Redis cache get failed, key={}", key, ex);
            return Optional.empty();
        }
    }

    private boolean isRedisTemporarilyUnavailable() {
        return System.currentTimeMillis() < redisUnavailableUntil.get();
    }

    private void markRedisFailure() {
        redisUnavailableUntil.set(System.currentTimeMillis() + FAILURE_BACKOFF_MILLIS);
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = redisTemplate.execute((RedisCallback<Set<String>>) connection -> scan(connection, pattern));
        return keys == null ? Set.of() : keys;
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
}
