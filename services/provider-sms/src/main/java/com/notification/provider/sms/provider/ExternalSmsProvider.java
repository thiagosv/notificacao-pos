package com.notification.provider.sms.provider;

import com.notification.provider.sms.dto.SmsRequest;
import com.notification.provider.sms.dto.SmsResponse;

/**
 * Interface comum para providers externos de SMS
 */
public interface ExternalSmsProvider {

    /**
     * Envia uma mensagem SMS
     * @param request requisição de SMS
     * @return resposta do provider
     */
    SmsResponse send(SmsRequest request);

    /**
     * Nome do provider (para logging e métricas)
     */
    String getProviderName();

    /**
     * Tipo do provider (PRIMARY ou SECONDARY)
     */
    ProviderType getProviderType();
}

