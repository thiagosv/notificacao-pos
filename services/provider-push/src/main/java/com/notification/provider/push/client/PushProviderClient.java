package com.notification.provider.push.client;

import com.notification.provider.push.dto.PushRequest;
import com.notification.provider.push.dto.PushResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushProviderClient {

    private final RestClient restClient = RestClient.create();
    private final Random random = new Random();

    @Value("${push-provider.url}")
    private String pushProviderUrl;

    @Value("${push-provider.error-rate:0.1}")
    private double errorRate;

    @Retryable(
        retryFor = {Exception.class},
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public PushResponse send(PushRequest request) {
        log.info("Sending push notification to: {}", request.getDeviceToken());

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
            if (response != null)
                log.info("Push sent successfully: messageId={}", response.getMessageId());
            return response;

        } catch (Exception e) {
            log.error("Failed to send push: {}", e.getMessage());
            throw new RuntimeException("Failed to send push notification", e);
        }
    }
}

