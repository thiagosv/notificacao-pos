package com.notification.provider.push.provider;

import com.notification.provider.push.dto.PushRequest;
import com.notification.provider.push.dto.PushResponse;

/**
 * Interface comum para providers externos de Push
 */
public interface ExternalPushProvider {

    /**
     * Envia uma notificação Push
     * @param request requisição de push
     * @return resposta do provider
     */
    PushResponse send(PushRequest request);

    /**
     * Nome do provider (para logging e métricas)
     */
    String getProviderName();

    /**
     * Tipo do provider (PRIMARY ou SECONDARY)
     */
    ProviderType getProviderType();
}

