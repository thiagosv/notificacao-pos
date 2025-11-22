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
 * Provider secundário de Email (Amazon SES simulado)
 * Maior custo, usado apenas como fallback
 */
@Slf4j
@Component
public class AmazonSesEmailProvider implements ExternalEmailProvider {

    private final RestClient restClient;
    private final String providerUrl;

    public AmazonSesEmailProvider(@Value("${email-provider.secondary.url}") String providerUrl) {
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
        log.info("[SECONDARY] Sending Email via Amazon SES to: {}", request.getEmail());

        try {
            EmailResponse response = restClient.post()
                    .uri(providerUrl)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(EmailResponse.class);

            if (response != null) {
                log.info("[SECONDARY] Email sent successfully via Amazon SES: messageId={}", response.getMessageId());
            }
            return response;

        } catch (Exception e) {
            log.error("[SECONDARY] Failed to send Email via Amazon SES: {}", e.getMessage());
            throw new RuntimeException("Amazon SES provider failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "Amazon_SES";
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.SECONDARY;
    }
}

