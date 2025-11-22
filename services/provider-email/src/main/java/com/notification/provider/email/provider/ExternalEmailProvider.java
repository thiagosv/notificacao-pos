package com.notification.provider.email.provider;

import com.notification.provider.email.dto.EmailRequest;
import com.notification.provider.email.dto.EmailResponse;

/**
 * Interface comum para providers externos de Email
 */
public interface ExternalEmailProvider {

    /**
     * Envia um email
     * @param request requisição de email
     * @return resposta do provider
     */
    EmailResponse send(EmailRequest request);

    /**
     * Nome do provider (para logging e métricas)
     */
    String getProviderName();

    /**
     * Tipo do provider (PRIMARY ou SECONDARY)
     */
    ProviderType getProviderType();
}

