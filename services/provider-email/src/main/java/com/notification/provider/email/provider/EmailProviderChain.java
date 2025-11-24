package com.notification.provider.email.provider;

import com.notification.provider.email.dto.EmailRequest;
import com.notification.provider.email.dto.EmailResponse;
import com.notification.provider.email.metrics.MetricsService;
import com.notification.provider.email.provider.impl.AmazonSesEmailProvider;
import com.notification.provider.email.provider.impl.SendGridEmailProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class EmailProviderChain {

    private final SendGridEmailProvider primaryProvider;
    private final AmazonSesEmailProvider secondaryProvider;
    private final CircuitBreaker primaryCircuitBreaker;
    private final CircuitBreaker secondaryCircuitBreaker;
    private final MetricsService metricsService;

    public EmailProviderChain(
            SendGridEmailProvider primaryProvider,
            AmazonSesEmailProvider secondaryProvider,
            CircuitBreakerRegistry circuitBreakerRegistry,
            MetricsService metricsService) {

        this.primaryProvider = primaryProvider;
        this.secondaryProvider = secondaryProvider;
        this.primaryCircuitBreaker = circuitBreakerRegistry.circuitBreaker("email-primary");
        this.secondaryCircuitBreaker = circuitBreakerRegistry.circuitBreaker("email-secondary");
        this.metricsService = metricsService;

        configureCircuitBreakerEvents();
    }

    public EmailResponse send(EmailRequest request) {
        log.info("Starting Email provider chain for: {}", request.getEmail());

        CircuitBreaker.State primaryState = primaryCircuitBreaker.getState();
        log.info("Primary Circuit Breaker state: {}", primaryState);

        if (primaryState != CircuitBreaker.State.OPEN && primaryState != CircuitBreaker.State.FORCED_OPEN) {
            try {
                log.info("Attempting to send via PRIMARY provider (SendGrid)");
                EmailResponse response = executeWithCircuitBreaker(primaryCircuitBreaker, () -> primaryProvider.send(request));

                log.info("✅ Email sent successfully via PRIMARY provider");
                return response;

            } catch (Exception e) {
                log.warn("⚠️ PRIMARY provider failed: {}. Falling back to SECONDARY...", e.getMessage());
            }
        } else {
            log.warn("⚠️ PRIMARY Circuit Breaker is OPEN. Skipping to SECONDARY provider...");
        }

        try {
            log.info("Attempting to send via SECONDARY provider (Amazon SES)");
            EmailResponse response = executeWithCircuitBreaker(secondaryCircuitBreaker, () -> secondaryProvider.send(request));

            log.info("✅ Email sent successfully via SECONDARY provider (FALLBACK)");
            metricsService.incrementFallback(primaryProvider.getProviderName(), secondaryProvider.getProviderName());
            return response;
        } catch (Exception e) {
            log.error("❌ SECONDARY provider also failed: {}", e.getMessage());
            throw new RuntimeException("All Email providers failed", e);
        }
    }

    private EmailResponse executeWithCircuitBreaker(CircuitBreaker circuitBreaker, Supplier<EmailResponse> operation) {
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

