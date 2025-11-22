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
 * Provider primário de SMS (Twilio simulado)
 * Menor custo, primeira opção
 */
@Slf4j
@Component
public class TwilioSmsProvider implements ExternalSmsProvider {

    private final RestClient restClient;
    private final String providerUrl;

    public TwilioSmsProvider(@Value("${sms-provider.primary.url}") String providerUrl) {
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
        log.info("[PRIMARY] Sending SMS via Twilio to: {}", request.getNumber());

        try {
            SmsResponse response = restClient.post()
                    .uri(providerUrl)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(SmsResponse.class);

            if (response != null) {
                log.info("[PRIMARY] SMS sent successfully via Twilio: messageId={}", response.getMessageId());
            }
            return response;

        } catch (Exception e) {
            log.error("[PRIMARY] Failed to send SMS via Twilio: {}", e.getMessage());
            throw new RuntimeException("Twilio provider failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "Twilio";
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.PRIMARY;
    }
}

