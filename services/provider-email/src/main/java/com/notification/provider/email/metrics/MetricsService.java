package com.notification.provider.email.metrics;

import com.notification.provider.email.dto.Channel;
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

    public void incrementFallback(String primaryProvider, String secondaryProvider) {
        Counter counter = counterCache.computeIfAbsent(buildCacheKey(primaryProvider, secondaryProvider), key ->
                Counter.builder("notification_provider_fallback_total")
                        .description("Total de fallback por canal e provedor")
                        .tag("channel", "email")
                        .tag("primaryProvider", normalize(primaryProvider))
                        .tag("secondaryProvider", normalize(secondaryProvider))
                        .register(meterRegistry)
        );

        counter.increment();
        log.debug("Metric incremented: notification_provider_fallback_total [primaryProvider={}, secondaryProvider={}]", primaryProvider, secondaryProvider);
    }

    private String buildCacheKey(String primaryProvider, String secondaryProvider) {
        return "notification_provider_fallback_total_%s_%s".formatted(
                normalize(primaryProvider),
                normalize(secondaryProvider)
        );
    }

    private String normalize(String valor) {
        if (valor == null)
            return "unknown";
        return valor.toLowerCase();
    }
}


