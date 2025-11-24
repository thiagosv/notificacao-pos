package com.notification.core.consumer;

import com.notification.core.dto.NotificationFailedEvent;
import com.notification.core.dto.NotificationSentEvent;
import com.notification.core.metrics.MetricsService;
import com.notification.core.model.Notification;
import com.notification.core.model.NotificationStatus;
import com.notification.core.service.NotificationOrchestrator;
import com.notification.core.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for notification events from provider services
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final NotificationOrchestrator notificationOrchestrator;
    private final MetricsService metricsService;

    @KafkaListener(
            topics = "${kafka.topics.notification-sent}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "notificationSentListenerFactory"
    )
    public void handleNotificationSent(NotificationSentEvent event, Acknowledgment acknowledgment) {
        log.info("Received notification sent event: notificationId={}, providerId={}", event.getNotificationId(), event.getProviderId());

        try {
            notificationService.updateStatusToSent(event);

            if (acknowledgment != null)
                acknowledgment.acknowledge();

            log.info("Notification marked as SENT: id={}", event.getNotificationId());
        } catch (Exception e) {
            log.error("Error processing sent event for notificationId={}: {}", event.getNotificationId(), e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.notification-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "notificationFailedListenerFactory"
    )
    public void handleNotificationFailed(NotificationFailedEvent event, Acknowledgment acknowledgment) {
        log.warn("Received notification failed event: notificationId={}, reason={}", event.getNotificationId(), event.getFailureReason());

        try {
            Notification notification = notificationService.handleFailure(
                    event.getNotificationId(),
                    event.getFailureReason()
            );

            // Se a notificação está em RETRYING, republica para nova tentativa
            if (notification.getStatus() == NotificationStatus.RETRYING) {
                log.info("Republishing notification for retry: id={}, retryCount={}",
                        notification.getId(), notification.getRetryCount());
                notificationOrchestrator.publishCreatedNotificationEvent(notification);
            } else {
                notificationOrchestrator.publishDlqNotificationEvent(notification);
                metricsService.incrementDlq(notification.getChannel(), "max_retries_exceeded");
            }

            if (acknowledgment != null)
                acknowledgment.acknowledge();

            log.info("Notification failure processed: id={}, status={}",
                    event.getNotificationId(), notification.getStatus());
        } catch (Exception e) {
            log.error("Error processing failed event for notificationId={}: {}", event.getNotificationId(), e.getMessage(), e);
        }
    }
}

