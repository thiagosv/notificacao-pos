package com.notification.provider.push.consumer;

import com.notification.provider.push.dto.Channel;
import com.notification.provider.push.dto.NotificationEvent;
import com.notification.provider.push.dto.PushResponse;
import com.notification.provider.push.service.NotificationEventPublisher;
import com.notification.provider.push.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationConsumer {

    private final PushNotificationService pushNotificationService;
    private final NotificationEventPublisher eventPublisher;

    @KafkaListener(
            topics = "${kafka.topics.notification-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleNotification(NotificationEvent event, @Header("channel") String channel, Acknowledgment ack) {
        if (!Channel.PUSH.name().equalsIgnoreCase(channel)) {
            log.trace("Skipping non-PUSH notification: channel={}", channel);
            ack.acknowledge();
            return;
        }

        log.info("Processing PUSH notification: id={}, recipient={}", event.getNotificationId(), event.getRecipient());

        try {
            PushResponse response = pushNotificationService.sendPushNotification(event);
            eventPublisher.publishNotificationSent(event.getNotificationId(), response.getMessageId());
            ack.acknowledge();
            log.info("PUSH notification sent successfully: id={}", event.getNotificationId());
        } catch (Exception e) {
            log.error("Failed to send PUSH after retries: id={}, error={}", event.getNotificationId(), e.getMessage());
            eventPublisher.publishNotificationFailed(event.getNotificationId(), e.getMessage());
            ack.acknowledge();
        }
    }
}

