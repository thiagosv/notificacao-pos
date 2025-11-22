package com.notification.provider.email.client;

import com.notification.provider.email.dto.EmailRequest;
import com.notification.provider.email.dto.EmailResponse;
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
public class EmailProviderClient {

    private final RestClient restClient;
    private final Random random = new Random();

    @Value("${email-provider.url}")
    private String emailProviderUrl;

    @Value("${email-provider.error-rate:0.1}")
    private double errorRate;

    public EmailProviderClient() {
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
    public EmailResponse send(EmailRequest request) {
        log.info("Sending email notification to: {} using URL: {}", request.getEmail(), emailProviderUrl);

        if (random.nextDouble() < errorRate) {
            log.error("Simulated random error (10% chance)");
            throw new RuntimeException("Email provider service temporarily unavailable");
        }

        try {
            EmailResponse response = restClient.post()
                    .uri(emailProviderUrl)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(EmailResponse.class);

            if (response != null) {
                log.info("Email sent successfully: messageId={}", response.getMessageId());
            }
            return response;

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", emailProviderUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}

