package com.notification.provider.email.provider.impl;

import com.notification.provider.email.dto.EmailRequest;
import com.notification.provider.email.dto.EmailResponse;
import com.notification.provider.email.provider.ExternalEmailProvider;
import com.notification.provider.email.provider.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Provider primário de Email (SendGrid simulado)
 * Menor custo, primeira opção
 */
@Slf4j
@Component
public class SendGridEmailProvider implements ExternalEmailProvider {

    private final RestClient restClient;
    private final String providerUrl;

    public SendGridEmailProvider(@Value("${email-provider.primary.url}") String providerUrl) {
        this.providerUrl = providerUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Override
    public EmailResponse send(EmailRequest request) {
        log.info("[PRIMARY] Sending Email via SendGrid to: {}", request.getEmail());

        try {
            EmailResponse response = restClient.post()
                    .uri(providerUrl)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(EmailResponse.class);

            if (response != null) {
                log.info("[PRIMARY] Email sent successfully via SendGrid: messageId={}", response.getMessageId());
            }
            return response;

        } catch (Exception e) {
            log.error("[PRIMARY] Failed to send Email via SendGrid: {}", e.getMessage());
            throw new RuntimeException("SendGrid provider failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "SendGrid";
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.PRIMARY;
    }
}

