package com.notification.provider.sms.client;

import com.notification.provider.sms.dto.SmsRequest;
import com.notification.provider.sms.dto.SmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Random;

@Slf4j
@Component
public class SmsProviderClient {

    private final RestClient restClient;
    private final Random random = new Random();

    @Value("${sms-provider.url}")
    private String smsProviderUrl;

    @Value("${sms-provider.error-rate:0.1}")
    private double errorRate;

    public SmsProviderClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Retryable(
            retryFor = {Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public SmsResponse send(SmsRequest request) {
        log.info("Sending sms notification to: {} using URL: {}", request.getNumber(), smsProviderUrl);

        if (random.nextDouble() < errorRate) {
            log.error("Simulated random error (10% chance)");
            throw new RuntimeException("Sms provider service temporarily unavailable");
        }

        try {
            SmsResponse response = restClient.post()
                    .uri(smsProviderUrl)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(SmsResponse.class);

            if (response != null) {
                log.info("Sms sent successfully: messageId={}", response.getMessageId());
            }
            return response;

        } catch (Exception e) {
            log.error("Failed to send sms to {}: {}", smsProviderUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to send sms notification", e);
        }
    }
}

