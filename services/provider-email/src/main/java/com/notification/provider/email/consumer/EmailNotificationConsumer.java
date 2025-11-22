package com.notification.provider.email.consumer;

import com.notification.provider.email.dto.Channel;
import com.notification.provider.email.dto.NotificationEvent;
import com.notification.provider.email.dto.EmailResponse;
import com.notification.provider.email.service.NotificationEventPublisher;
import com.notification.provider.email.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final EmailNotificationService emailNotificationService;
    private final NotificationEventPublisher eventPublisher;

    @KafkaListener(
            topics = "${kafka.topics.notification-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleNotification(NotificationEvent event, @Header("channel") String channel, Acknowledgment ack) {
        if (!Channel.EMAIL.name().equalsIgnoreCase(channel)) {
            log.trace("Skipping non-EMAIL notification: channel={}", channel);
            ack.acknowledge();
            return;
        }

        log.info("Processing EMAIL notification: id={}, recipient={}", event.getNotificationId(), event.getRecipient());

        try {
            EmailResponse response = emailNotificationService.sendEmailNotification(event);
            eventPublisher.publishNotificationSent(event.getNotificationId(), response.getMessageId());
            ack.acknowledge();
            log.info("EMAIL notification sent successfully: id={}", event.getNotificationId());
        } catch (Exception e) {
            log.error("Failed to send EMAIL after retries: id={}, error={}", event.getNotificationId(), e.getMessage());
            eventPublisher.publishNotificationFailed(event.getNotificationId(), e.getMessage());
            ack.acknowledge();
        }
    }
}

