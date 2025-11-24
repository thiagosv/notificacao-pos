package com.notification.core.service;

import com.notification.core.dto.NotificationEvent;
import com.notification.core.dto.NotificationRequest;
import com.notification.core.dto.NotificationResponse;
import com.notification.core.exception.QuotaExceededException;
import com.notification.core.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOrchestrator {

    private final IdempotencyService idempotencyService;
    private final QuotaValidationService quotaValidationService;
    private final NotificationService notificationService;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${kafka.topics.notification-created}")
    private String notificactionCreatedTopic;

    @Value("${kafka.topics.notification-failed-dlq}")
    private String notificactionDlqTopic;

    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Processing notification request: idempotencyKey={}, clientId={}, channel={}",
                request.getIdempotencyKey(), request.getClientId(), request.getChannel());

        Optional<Notification> existing = idempotencyService.findByIdempotencyKey(request.getIdempotencyKey());

        if (existing.isPresent()) {
            log.warn("Duplicate notification detected: idempotencyKey={}, existingId={}",
                    request.getIdempotencyKey(), existing.get().getId());
            return NotificationResponse.of(existing.get());
        }

        boolean hasQuota = quotaValidationService.validateAndConsumeQuota(
                request.getClientId(),
                request.getChannel(),
                1L,
                request.getIdempotencyKey()
        );

        if (!hasQuota) {
            log.error("Quota exceeded: clientId={}, channel={}", request.getClientId(), request.getChannel());
            throw new QuotaExceededException(
                    String.format("Quota exceeded for client %s on channel %s", request.getClientId(), request.getChannel())
            );
        }

        Notification notification = notificationService.createNotification(request);

        idempotencyService.register(request.getIdempotencyKey(), notification.getId());

        publishNotificationEvent(notification, notificactionCreatedTopic);

        log.info("Notification successfully processed: id={}, status={}", notification.getId(), notification.getStatus());

        return NotificationResponse.of(notification);
    }

    public void publishNotificationEvent(Notification notification, String notificactionCreatedTopic) {
        try {
            NotificationEvent event = NotificationEvent.of(notification);
            Message<NotificationEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, notificactionCreatedTopic)
                    .setHeader(KafkaHeaders.KEY, notification.getId())
                    .setHeader("channel", notification.getChannel().name())
                    .setHeader("priority", notification.getPriority().name())
                    .setHeader("clientId", notification.getClientId())
                    .build();

            kafkaTemplate.send(message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish notification event: id={}, error={}", notification.getId(), ex.getMessage(), ex);
                        } else {
                            log.info("Notification event published successfully: id={}, topic={}, partition={}",
                                    notification.getId(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing notification event: id={}, error={}", notification.getId(), e.getMessage(), e);
        }
    }

    public void publishCreatedNotificationEvent(Notification notification) {
        publishNotificationEvent(notification, notificactionCreatedTopic);
    }

    public void publishDlqNotificationEvent(Notification notification) {
        publishNotificationEvent(notification, notificactionDlqTopic);
    }
}

