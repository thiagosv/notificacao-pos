package com.notification.provider.sms.consumer;

import com.notification.provider.sms.dto.Channel;
import com.notification.provider.sms.dto.NotificationEvent;
import com.notification.provider.sms.dto.SmsResponse;
import com.notification.provider.sms.service.NotificationEventPublisher;
import com.notification.provider.sms.service.SmsNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationConsumer {

    private final SmsNotificationService smsNotificationService;
    private final NotificationEventPublisher eventPublisher;

    @KafkaListener(
            topics = "${kafka.topics.notification-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleNotification(NotificationEvent event, @Header("channel") String channel, Acknowledgment ack) {
        if (!Channel.SMS.name().equalsIgnoreCase(channel)) {
            log.trace("Skipping non-SMS notification: channel={}", channel);
            ack.acknowledge();
            return;
        }

        log.info("Processing SMS notification: id={}, recipient={}", event.getNotificationId(), event.getRecipient());

        try {
            SmsResponse response = smsNotificationService.sendSmsNotification(event);
            eventPublisher.publishNotificationSent(event.getNotificationId(), response.getMessageId());
            ack.acknowledge();
            log.info("SMS notification sent successfully: id={}", event.getNotificationId());
        } catch (Exception e) {
            log.error("Failed to send SMS after retries: id={}, error={}", event.getNotificationId(), e.getMessage());
            eventPublisher.publishNotificationFailed(event.getNotificationId(), e.getMessage());
            ack.acknowledge();
        }
    }
}

