package com.notification.template.service;

import com.notification.template.model.Channel;
import com.notification.template.model.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateCacheService {

    private final RedisTemplate<String, Template> redisTemplate;

    @Value("${cache.template.ttl:3600}")
    private long ttl;

    private String buildCacheKey(String clientId, Channel channel, String templateCode) {
        return String.format("template:%s:%s:%s", clientId, channel, templateCode);
    }

    public Optional<Template> get(String clientId, Channel channel, String templateCode) {
        String key = buildCacheKey(clientId, channel, templateCode);

        try {
            Template template = redisTemplate.opsForValue().get(key);

            if (template != null) {
                log.debug("Cache HIT for key: {}", key);
                return Optional.of(template);
            }

            log.debug("Cache MISS for key: {}", key);
            return Optional.empty();

        } catch (Exception e) {
            log.warn("Error reading from cache, key: {}, error: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    public void put(String clientId, Channel channel, String templateCode, Template template) {
        String key = buildCacheKey(clientId, channel, templateCode);

        try {
            redisTemplate.opsForValue().set(key, template, Duration.ofSeconds(ttl));
            log.debug("Template cached with key: {}, TTL: {}s", key, ttl);

        } catch (Exception e) {
            log.warn("Error writing to cache, key: {}, error: {}", key, e.getMessage());
        }
    }

    public void evict(String clientId, Channel channel, String templateCode) {
        String key = buildCacheKey(clientId, channel, templateCode);

        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("Cache evicted for key: {}, deleted: {}", key, deleted);

        } catch (Exception e) {
            log.warn("Error evicting cache, key: {}, error: {}", key, e.getMessage());
        }
    }
}

