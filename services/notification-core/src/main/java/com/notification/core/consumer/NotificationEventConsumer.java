package com.notification.core.consumer;

import com.notification.core.dto.NotificationFailedEvent;
import com.notification.core.dto.NotificationSentEvent;
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

    @KafkaListener(
            topics = "${kafka.topics.notification-sent}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNotificationSent(NotificationSentEvent event, Acknowledgment acknowledgment) {
        log.info("Received notification sent event: notificationId={}, providerId={}", event.getNotificationId(), event.getProviderId());

        try {
            notificationService.updateStatusToSent(
                    event.getNotificationId(),
                    event.getProviderId(),
                    event.getProviderMessageId()
            );

            if (acknowledgment != null)
                acknowledgment.acknowledge();

            log.info("Notification marked as SENT: id={}", event.getNotificationId());
        } catch (Exception e) {
            log.error("Error processing sent event for notificationId={}: {}", event.getNotificationId(), e.getMessage(), e);
        }
    }

    /**
     * Handle notification failed event (from providers)
     */
    @KafkaListener(
            topics = "${kafka.topics.notification-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNotificationFailed(NotificationFailedEvent event, Acknowledgment acknowledgment) {
        log.warn("Received notification failed event: notificationId={}, reason={}", event.getNotificationId(), event.getFailureReason());

        try {
            notificationService.handleFailure(
                    event.getNotificationId(),
                    event.getFailureReason()
            );

            if (acknowledgment != null)
                acknowledgment.acknowledge();

            log.info("Notification failure processed: id={}", event.getNotificationId());
        } catch (Exception e) {
            log.error("Error processing failed event for notificationId={}: {}", event.getNotificationId(), e.getMessage(), e);
        }
    }
}

