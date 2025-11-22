package com.notification.provider.sms.service;

import com.notification.provider.sms.dto.NotificationFailedEvent;
import com.notification.provider.sms.dto.NotificationSentEvent;
import com.notification.provider.sms.dto.SmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.notification-sent}")
    private String notificationSentTopic;

    @Value("${kafka.topics.notification-failed}")
    private String notificationFailedTopic;

    public void publishNotificationSent(String notificationId, SmsResponse response) {
        NotificationSentEvent event = NotificationSentEvent.builder()
                .notificationId(notificationId)
                .providerId(response.getProvider())
                .providerMessageId(response.getMessageId())
                .cost(response.getCost() != null ? response.getCost() : BigDecimal.ZERO)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(notificationSentTopic, notificationId, event);
        log.info("Published notification.sent: id={}", notificationId);
    }

    public void publishNotificationFailed(String notificationId, String failureReason) {
        NotificationFailedEvent event = NotificationFailedEvent.builder()
                .notificationId(notificationId)
                .failureReason(failureReason)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(notificationFailedTopic, notificationId, event);
        log.error("Published notification.failed: id={}, reason={}", notificationId, failureReason);
    }
}

