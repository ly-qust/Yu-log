package com.yu.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.blog.common.cache.CacheProperties;
import com.yu.blog.common.cache.RedisCacheService;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisCacheServiceUnitTests {

    @Test
    void shouldDegradeWhenRedisGetFails() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

        RedisCacheService cacheService = new RedisCacheService(redisTemplate, new ObjectMapper(), new CacheProperties());

        assertThat(cacheService.get("yu-log:test", String.class)).isEmpty();
    }

    @Test
    void shouldSkipRedisOperationsDuringFailureBackoff() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

        RedisCacheService cacheService = new RedisCacheService(redisTemplate, new ObjectMapper(), new CacheProperties());
        cacheService.get("yu-log:test", String.class);
        cacheService.put("yu-log:test", "value", Duration.ofMinutes(1));

        verify(redisTemplate).opsForValue();
        verifyNoMoreInteractions(redisTemplate);
    }

    @Test
    void shouldDegradeWhenRedisEvictFails() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.delete("yu-log:test")).thenThrow(new RuntimeException("redis down"));

        RedisCacheService cacheService = new RedisCacheService(redisTemplate, new ObjectMapper(), new CacheProperties());

        cacheService.evict("yu-log:test");
    }
}
