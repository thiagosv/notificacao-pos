package com.notification.provider.push.client;

import com.notification.provider.push.dto.PushRequest;
import com.notification.provider.push.dto.PushResponse;
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
public class PushProviderClient {

    private final RestClient restClient;
    private final Random random = new Random();

    @Value("${push-provider.url}")
    private String pushProviderUrl;

    @Value("${push-provider.error-rate:0.1}")
    private double errorRate;

    public PushProviderClient() {
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
    public PushResponse send(PushRequest request) {
        log.info("Sending push notification to: {} using URL: {}", request.getDeviceToken(), pushProviderUrl);

        if (random.nextDouble() < errorRate) {
            log.error("Simulated random error (10% chance)");
            throw new RuntimeException("Push provider service temporarily unavailable");
        }

        try {
            PushResponse response = restClient.post()
                .uri(pushProviderUrl)
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(PushResponse.class);

            if (response != null) {
                log.info("Push sent successfully: messageId={}", response.getMessageId());
            }
            return response;

        } catch (Exception e) {
            log.error("Failed to send push to {}: {}", pushProviderUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to send push notification", e);
        }
    }
}

