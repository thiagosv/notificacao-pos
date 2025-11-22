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
 * Provider primário de Push (Firebase FCM simulado)
 * Menor custo, primeira opção
 */
@Slf4j
@Component
public class FirebaseFcmPushProvider implements ExternalPushProvider {

    private final RestClient restClient;
    private final String providerUrl;

    public FirebaseFcmPushProvider(@Value("${push-provider.primary.url}") String providerUrl) {
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
        log.info("[PRIMARY] Sending Push via Firebase FCM to: {}", request.getDeviceToken());

        try {
            PushResponse response = restClient.post()
                    .uri(providerUrl)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(PushResponse.class);

            if (response != null) {
                log.info("[PRIMARY] Push sent successfully via Firebase FCM: messageId={}", response.getMessageId());
            }
            return response;

        } catch (Exception e) {
            log.error("[PRIMARY] Failed to send Push via Firebase FCM: {}", e.getMessage());
            throw new RuntimeException("Firebase FCM provider failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "Firebase_FCM";
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.PRIMARY;
    }
}

