package com.notification.core.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IdempotencyRepository {

    private static final String KEY_PREFIX = "idempotency:";

    private final StringRedisTemplate redisTemplate;

    public void register(String idempotencyKey, String notificationId, long ttl) {
        String key = KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, notificationId, Duration.ofSeconds(ttl));
        log.debug("Registered idempotency key: {} -> {}", idempotencyKey, notificationId);
    }

    public Optional<String> findNotificationId(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        String notificationId = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(notificationId);
    }

    public boolean exists(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        redisTemplate.delete(key);
        log.debug("Deleted idempotency key: {}", idempotencyKey);
    }
}

