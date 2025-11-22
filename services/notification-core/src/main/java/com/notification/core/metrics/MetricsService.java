package com.notification.core.metrics;

import com.notification.core.dto.NotificationSentEvent;
import com.notification.core.model.Channel;
import com.notification.core.model.Notification;
import com.notification.core.model.NotificationStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counterCache;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.counterCache = new ConcurrentHashMap<>();
        log.info("MetricsService initialized");
    }

    public void incrementNotificationCounter(Channel channel, NotificationStatus status, String provider) {
        String cacheKey = buildCacheKey(channel, status, provider);

        Counter counter = counterCache.computeIfAbsent(cacheKey, key ->
                Counter.builder("notification_total")
                        .description("Total de notificações por canal e status")
                        .tag("channel", normalize(channel))
                        .tag("status", normalize(status))
                        .tag("provider", normalize(provider))
                        .register(meterRegistry)
        );

        counter.increment();
        log.debug("Metric incremented: notification_total [channel={}, status={}]", channel, status);
    }

    public void incrementNotificationCounter(Channel channel, NotificationStatus status) {
        incrementNotificationCounter(channel, status, "");
    }

    public void incrementNotificationCostCounter(Channel channel, String provider, BigDecimal cost) {
        String cacheKey = buildCacheKey(channel, provider, cost.toString());

        Counter counter = counterCache.computeIfAbsent(cacheKey, key ->
                Counter.builder("notification_cost_total")
                        .description("Custo de notificações, por canal e provedor")
                        .tag("channel", normalize(channel))
                        .tag("provider", normalize(provider))
                        .register(meterRegistry)
        );

        counter.increment(cost.doubleValue());
        log.debug("Metric incremented: notification_total [channel={}, provider={}, cost={}]", channel, provider, cost);
    }

    public void recordLatency(Channel channel, String provider, Duration duration) {
        meterRegistry.timer("notification.delivery.time",
                Tags.of(
                        "channel", normalize(channel),
                        "provider", normalize(provider)
                )).record(duration);
    }

    public void incrementSent(Notification notification, NotificationSentEvent event) {
        incrementNotificationCounter(notification.getChannel(), NotificationStatus.SENT, event.getProviderId());
        incrementNotificationCostCounter(notification.getChannel(), event.getProviderId(), event.getCost());
        recordLatency(notification.getChannel(), event.getProviderId(), Duration.between(notification.getCreatedAt(), notification.getUpdatedAt()));
    }

    public void incrementFailed(Channel channel) {
        incrementNotificationCounter(channel, NotificationStatus.FAILED);
    }

    public void incrementPending(Channel channel) {
        incrementNotificationCounter(channel, NotificationStatus.PENDING);
    }

    private String normalize(Enum<?> enumValue) {
        if (enumValue == null)
            return "unknown";
        return enumValue.name().toLowerCase();
    }

    private String normalize(String valor) {
        if (valor == null)
            return "unknown";
        return valor.toLowerCase();
    }

    private String buildCacheKey(Channel channel, NotificationStatus status, String provider) {
        return "notification_total_%s_%s_%s".formatted(
                normalize(channel),
                normalize(status),
                provider
        );
    }

    private String buildCacheKey(Channel channel, String provider, String cost) {
        return "notification_cost_total_%s_%s_%s".formatted(
                normalize(channel),
                provider,
                cost
        );
    }

    public Map<String, Double> getCurrentCounts() {
        Map<String, Double> counts = new ConcurrentHashMap<>();
        counterCache.forEach((key, counter) -> counts.put(key, counter.count()));
        return counts;
    }
}


