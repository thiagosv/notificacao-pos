package com.notification.provider.sms.provider;

import com.notification.provider.sms.dto.SmsRequest;
import com.notification.provider.sms.dto.SmsResponse;
import com.notification.provider.sms.provider.impl.AwsSnsSmsProvider;
import com.notification.provider.sms.provider.impl.TwilioSmsProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class SmsProviderChain {

    private final TwilioSmsProvider primaryProvider;
    private final AwsSnsSmsProvider secondaryProvider;
    private final CircuitBreaker primaryCircuitBreaker;
    private final CircuitBreaker secondaryCircuitBreaker;

    public SmsProviderChain(
            TwilioSmsProvider primaryProvider,
            AwsSnsSmsProvider secondaryProvider,
            CircuitBreakerRegistry circuitBreakerRegistry) {

        this.primaryProvider = primaryProvider;
        this.secondaryProvider = secondaryProvider;
        this.primaryCircuitBreaker = circuitBreakerRegistry.circuitBreaker("sms-primary");
        this.secondaryCircuitBreaker = circuitBreakerRegistry.circuitBreaker("sms-secondary");

        configureCircuitBreakerEvents();
    }

    public SmsResponse send(SmsRequest request) {
        log.info("Starting SMS provider chain for: {}", request.getNumber());

        // Verifica estado do circuit breaker primário
        CircuitBreaker.State primaryState = primaryCircuitBreaker.getState();
        log.info("Primary Circuit Breaker state: {}", primaryState);

        // Se o primary estiver disponível (CLOSED ou HALF_OPEN), tenta usar
        if (primaryState != CircuitBreaker.State.OPEN && primaryState != CircuitBreaker.State.FORCED_OPEN) {

            try {
                log.info("Attempting to send via PRIMARY provider (Twilio)");
                SmsResponse response = executeWithCircuitBreaker(primaryCircuitBreaker, () -> primaryProvider.send(request));

                log.info("✅ SMS sent successfully via PRIMARY provider");
                return response;
            } catch (Exception e) {
                log.warn("⚠️ PRIMARY provider failed: {}. Falling back to SECONDARY...", e.getMessage());
            }
        } else {
            log.warn("⚠️ PRIMARY Circuit Breaker is OPEN. Skipping to SECONDARY provider...");
        }

        try {
            log.info("Attempting to send via SECONDARY provider (AWS SNS)");
            SmsResponse response = executeWithCircuitBreaker(secondaryCircuitBreaker, () -> secondaryProvider.send(request));

            log.info("✅ SMS sent successfully via SECONDARY provider (FALLBACK)");
            return response;
        } catch (Exception e) {
            log.error("❌ SECONDARY provider also failed: {}", e.getMessage());
            throw new RuntimeException("All SMS providers failed", e);
        }
    }

    private SmsResponse executeWithCircuitBreaker(CircuitBreaker circuitBreaker, Supplier<SmsResponse> operation) {
        return CircuitBreaker.decorateSupplier(circuitBreaker, operation).get();
    }

    private void configureCircuitBreakerEvents() {
        primaryCircuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("🔄 [PRIMARY CB] State transition: {} -> {}",
                                event.getStateTransition().getFromState(), event.getStateTransition().getToState()))
                .onFailureRateExceeded(event ->
                        log.error("⚠️ [PRIMARY CB] Failure rate exceeded: {}%", event.getFailureRate()))
                .onError(event ->
                        log.error("❌ [PRIMARY CB] Error occurred: {}", event.getThrowable().getMessage()));

        secondaryCircuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("🔄 [SECONDARY CB] State transition: {} -> {}",
                                event.getStateTransition().getFromState(), event.getStateTransition().getToState()))
                .onFailureRateExceeded(event ->
                        log.error("⚠️ [SECONDARY CB] Failure rate exceeded: {}%", event.getFailureRate()))
                .onError(event ->
                        log.error("❌ [SECONDARY CB] Error occurred: {}", event.getThrowable().getMessage()));
    }

}

