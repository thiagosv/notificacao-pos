package com.notification.quota.repository;

import com.notification.quota.model.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class QuotaCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${quota.cache.ttl:3600}")
    private Long cacheTtl;

    private static final String QUOTA_KEY_PREFIX = "quota:";

    public void saveAvailableQuota(String clientId, Channel channel, Long availableQuota) {
        String key = buildKey(clientId, channel);
        redisTemplate.opsForValue().set(key, availableQuota, Duration.ofSeconds(cacheTtl));
        log.debug("Cached quota for clientId={}, channel={}, available={}", clientId, channel, availableQuota);
    }

    public Optional<Long> getAvailableQuota(String clientId, Channel channel) {
        String key = buildKey(clientId, channel);
        Object value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            log.debug("Cache HIT for clientId={}, channel={}", clientId, channel);
            return Optional.of(Long.parseLong(value.toString()));
        }

        log.debug("Cache MISS for clientId={}, channel={}", clientId, channel);
        return Optional.empty();
    }

    public void decrementQuota(String clientId, Channel channel, Long amount) {
        String key = buildKey(clientId, channel);
        Long result = redisTemplate.opsForValue().decrement(key, amount);
        log.debug("Decremented quota for clientId={}, channel={}, amount={}, newValue={}",
                clientId, channel, amount, result);
    }

    public void incrementQuota(String clientId, Channel channel, Long amount) {
        String key = buildKey(clientId, channel);
        Long result = redisTemplate.opsForValue().increment(key, amount);
        log.debug("Incremented quota for clientId={}, channel={}, amount={}, newValue={}",
                clientId, channel, amount, result);
    }

    public void invalidate(String clientId, Channel channel) {
        String key = buildKey(clientId, channel);
        redisTemplate.delete(key);
        log.debug("Invalidated cache for clientId={}, channel={}", clientId, channel);
    }

    public void invalidateAll(String clientId) {
        for (Channel channel : Channel.values()) {
            invalidate(clientId, channel);
        }
        log.debug("Invalidated all cache for clientId={}", clientId);
    }

    private String buildKey(String clientId, Channel channel) {
        return QUOTA_KEY_PREFIX + clientId + ":" + channel.name();
    }

}