package com.notification.quota.service;

import com.notification.quota.model.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaEventHandler {

    private final QuotaValidationService quotaValidationService;

    public void handleNotificationSent(String clientId, String channel, String notificationId) {
        log.info("Handling notification sent event: clientId={}, channel={}, notificationId={}",
                clientId, channel, notificationId);

        // Quota já foi consumida no momento da validação
        // Este handler pode ser usado para métricas/auditoria adicional
        // Preparação sprint 2
        log.debug("Notification sent successfully, quota was already consumed");
    }

    public void handleNotificationFailed(String clientId, String channel, String notificationId, Long amount) {
        log.info("Handling notification failed event: clientId={}, channel={}, notificationId={}",
                clientId, channel, notificationId);

        try {
            // Liberar a quota que foi consumida
            quotaValidationService.releaseQuota(
                    clientId,
                    Channel.valueOf(channel.toUpperCase()),
                    amount,
                    notificationId
            );

            log.info("Quota released due to notification failure");
        } catch (Exception e) {
            log.error("Error releasing quota for failed notification: {}", e.getMessage(), e);
        }
    }

}