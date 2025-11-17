package com.notification.core.config;

import com.notification.core.model.Channel;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Observability configuration - Metrics and tracing
 */
@Configuration
public class ObservabilityConfig {

    private static final String CANAL_TAG = "channel";

    @Bean
    public CustomMetrics customMetrics(MeterRegistry meterRegistry) {
        return new CustomMetrics(meterRegistry);
    }

    public static class CustomMetrics {
        private final MeterRegistry meterRegistry;

        public CustomMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        public void incrementNotificationCreated(Channel channel) {
            meterRegistry.counter("notification.created", CANAL_TAG, channel.name()).increment();
        }

        public void incrementNotificationSent(Channel channel) {
            meterRegistry.counter("notification.sent", CANAL_TAG, channel.name()).increment();
        }

        public void incrementNotificationFailed(Channel channel, String reason) {
            meterRegistry.counter("notification.failed",
                CANAL_TAG, channel.name(),
                "reason", reason).increment();
        }

        public void incrementQuotaValidation(String result) {
            meterRegistry.counter("quota.validation", "result", result).increment();
        }

        public void incrementIdempotencyHit() {
            meterRegistry.counter("idempotency.hit").increment();
        }
    }
}

