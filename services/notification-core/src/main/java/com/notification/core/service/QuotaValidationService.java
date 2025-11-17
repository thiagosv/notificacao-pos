package com.notification.core.service;

import com.notification.core.dto.QuotaValidationRequest;
import com.notification.core.dto.QuotaValidationResponse;
import com.notification.core.exception.QuotaServiceException;
import com.notification.core.model.Channel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaValidationService {

    private final RestClient restClient;

    @Value("${quota-service.url}")
    private String quotaServiceUrl;

    @CircuitBreaker(name = "quotaService", fallbackMethod = "quotaValidationFallback")
    @Retry(name = "quotaService")
    public boolean validateAndConsumeQuota(String clientId, Channel channel, Long amount, String notificationId) {
        log.info("Validating quota: clientId={}, channel={}, amount={}, notificationId={}",
                clientId, channel, amount, notificationId);

        try {
            QuotaValidationRequest request = QuotaValidationRequest.builder()
                    .clientId(clientId)
                    .channel(channel)
                    .amount(amount)
                    .notificationId(notificationId)
                    .build();

            QuotaValidationResponse response = restClient.post()
                    .uri(quotaServiceUrl + "/quotas/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) ->
                            log.warn("Quota validation failed: status={}", res.getStatusCode()))
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Quota service error: status={}", res.getStatusCode());
                        throw new QuotaServiceException("Quota service returned error: " + res.getStatusCode());
                    })
                    .body(QuotaValidationResponse.class);

            if (response != null && response.getAllowed() != null) {
                boolean allowed = response.getAllowed();
                log.info("Quota validation result: allowed={}, available={}", allowed, response.getAvailableQuota());
                return allowed;
            }

            log.warn("Invalid response from quota service");
            return false;

        } catch (QuotaServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating quota: {}", e.getMessage(), e);
            throw new QuotaServiceException("Failed to validate quota", e);
        }
    }
}

