package com.notification.provider.sms.provider.impl;

import com.notification.provider.sms.dto.SmsRequest;
import com.notification.provider.sms.dto.SmsResponse;
import com.notification.provider.sms.provider.ExternalSmsProvider;
import com.notification.provider.sms.provider.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Provider secundário de SMS (AWS SNS simulado)
 * Maior custo, usado apenas como fallback
 */
@Slf4j
@Component
public class AwsSnsSmsProvider implements ExternalSmsProvider {

    private final RestClient restClient;
    private final String providerUrl;

    public AwsSnsSmsProvider(@Value("${sms-provider.secondary.url}") String providerUrl) {
        this.providerUrl = providerUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Override
    public SmsResponse send(SmsRequest request) {
        log.info("[SECONDARY] Sending SMS via AWS SNS to: {}", request.getNumber());

        try {
            SmsResponse response = restClient.post()
                    .uri(providerUrl)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(SmsResponse.class);

            if (response != null)
                log.info("[SECONDARY] SMS sent successfully via AWS SNS: messageId={}", response.getMessageId());
            return response;

        } catch (Exception e) {
            log.error("[SECONDARY] Failed to send SMS via AWS SNS: {}", e.getMessage());
            throw new RuntimeException("AWS SNS provider failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "AWS_SNS";
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.SECONDARY;
    }
}

