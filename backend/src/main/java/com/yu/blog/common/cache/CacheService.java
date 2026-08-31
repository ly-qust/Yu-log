package com.yu.blog.common.cache;

import com.yu.blog.common.api.PageResult;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface CacheService {
    <T> Optional<T> get(String key, Class<T> type);

    <T> Optional<List<T>> getList(String key, Class<T> itemType);

    <T> Optional<PageResult<T>> getPage(String key, Class<T> itemType);

    void put(String key, Object value, Duration ttl);

    void evict(String key);

    void evictByPattern(String pattern);
}
