package com.notification.provider.push.provider.impl;

import com.notification.provider.push.dto.PushRequest;
import com.notification.provider.push.dto.PushResponse;
import com.notification.provider.push.provider.ExternalPushProvider;
import com.notification.provider.push.provider.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Provider secundário de Push (OneSignal simulado)
 * Maior custo, usado apenas como fallback
 */
@Slf4j
@Component
public class OneSignalPushProvider implements ExternalPushProvider {

    private final RestClient restClient;
    private final String providerUrl;

    public OneSignalPushProvider(@Value("${push-provider.secondary.url}") String providerUrl) {
        this.providerUrl = providerUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Override
    public PushResponse send(PushRequest request) {
        log.info("[SECONDARY] Sending Push via OneSignal to: {}", request.getDeviceToken());

        try {
            PushResponse response = restClient.post()
                    .uri(providerUrl)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(PushResponse.class);

            if (response != null) {
                log.info("[SECONDARY] Push sent successfully via OneSignal: messageId={}", response.getMessageId());
            }
            return response;

        } catch (Exception e) {
            log.error("[SECONDARY] Failed to send Push via OneSignal: {}", e.getMessage());
            throw new RuntimeException("OneSignal provider failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "OneSignal";
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.SECONDARY;
    }
}

