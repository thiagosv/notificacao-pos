package com.notification.provider.audit.metrics;

import com.notification.provider.audit.model.NotificationStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public void incrementEventStored(NotificationStatus notificationStatus) {
        Counter counter = counterCache.computeIfAbsent(buildCacheKey(notificationStatus), key ->
                Counter.builder("event_store_events_total")
                        .description("Total de fallback por canal e provedor")
                        .tag("status", normalize(notificationStatus))
                        .register(meterRegistry)
        );

        counter.increment();
        log.debug("Metric incremented: notification_provider_fallback_total [notificationStatus={}]", notificationStatus.name());
    }

    private String buildCacheKey(NotificationStatus notificationStatus) {
        return "event_store_events_total_%s".formatted(normalize(notificationStatus));
    }

    private String normalize(Enum<?> enumValue) {
        if (enumValue == null)
            return "unknown";
        return enumValue.name().toLowerCase();
    }
}


