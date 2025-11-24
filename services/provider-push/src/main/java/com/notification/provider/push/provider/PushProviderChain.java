package com.notification.provider.push.provider;

import com.notification.provider.push.dto.PushRequest;
import com.notification.provider.push.dto.PushResponse;
import com.notification.provider.push.metrics.MetricsService;
import com.notification.provider.push.provider.impl.FirebaseFcmPushProvider;
import com.notification.provider.push.provider.impl.OneSignalPushProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class PushProviderChain {

    private final FirebaseFcmPushProvider primaryProvider;
    private final OneSignalPushProvider secondaryProvider;
    private final CircuitBreaker primaryCircuitBreaker;
    private final CircuitBreaker secondaryCircuitBreaker;
    private final MetricsService metricsService;

    public PushProviderChain(
            FirebaseFcmPushProvider primaryProvider,
            OneSignalPushProvider secondaryProvider,
            CircuitBreakerRegistry circuitBreakerRegistry,
            MetricsService metricsService) {

        this.primaryProvider = primaryProvider;
        this.secondaryProvider = secondaryProvider;
        this.primaryCircuitBreaker = circuitBreakerRegistry.circuitBreaker("push-primary");
        this.secondaryCircuitBreaker = circuitBreakerRegistry.circuitBreaker("push-secondary");
        this.metricsService = metricsService;

        configureCircuitBreakerEvents();
    }

    public PushResponse send(PushRequest request) {
        log.info("Starting Push provider chain for: {}", request.getDeviceToken());

        CircuitBreaker.State primaryState = primaryCircuitBreaker.getState();
        log.info("Primary Circuit Breaker state: {}", primaryState);

        if (primaryState != CircuitBreaker.State.OPEN && primaryState != CircuitBreaker.State.FORCED_OPEN) {
            try {
                log.info("Attempting to send via PRIMARY provider (Firebase FCM)");
                PushResponse response = executeWithCircuitBreaker(primaryCircuitBreaker, () -> primaryProvider.send(request));

                log.info("✅ Push sent successfully via PRIMARY provider");
                return response;
            } catch (Exception e) {
                log.warn("⚠️ PRIMARY provider failed: {}. Falling back to SECONDARY...", e.getMessage());
            }
        } else {
            log.warn("⚠️ PRIMARY Circuit Breaker is OPEN. Skipping to SECONDARY provider...");
        }

        try {
            log.info("Attempting to send via SECONDARY provider (OneSignal)");
            PushResponse response = executeWithCircuitBreaker(secondaryCircuitBreaker, () -> secondaryProvider.send(request));

            log.info("✅ Push sent successfully via SECONDARY provider (FALLBACK)");
            metricsService.incrementFallback(primaryProvider.getProviderName(), secondaryProvider.getProviderName());
            return response;
        } catch (Exception e) {
            log.error("❌ SECONDARY provider also failed: {}", e.getMessage());
            throw new RuntimeException("All Push providers failed", e);
        }
    }

    private PushResponse executeWithCircuitBreaker(CircuitBreaker circuitBreaker, Supplier<PushResponse> operation) {
        return CircuitBreaker.decorateSupplier(circuitBreaker, operation).get();
    }

    private void configureCircuitBreakerEvents() {
        primaryCircuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("🔄 [PRIMARY CB] State transition: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onFailureRateExceeded(event ->
                        log.error("⚠️ [PRIMARY CB] Failure rate exceeded: {}%", event.getFailureRate()))
                .onError(event ->
                        log.error("❌ [PRIMARY CB] Error occurred: {}", event.getThrowable().getMessage()));

        secondaryCircuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("🔄 [SECONDARY CB] State transition: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onFailureRateExceeded(event ->
                        log.error("⚠️ [SECONDARY CB] Failure rate exceeded: {}%", event.getFailureRate()))
                .onError(event ->
                        log.error("❌ [SECONDARY CB] Error occurred: {}", event.getThrowable().getMessage()));
    }

}

