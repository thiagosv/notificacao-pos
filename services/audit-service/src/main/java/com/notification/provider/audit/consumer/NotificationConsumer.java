package com.notification.provider.audit.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.provider.audit.dto.NotificationDto;
import com.notification.provider.audit.service.AuditNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final AuditNotificationService auditNotificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "#{'${kafka.topics.notification}'.split(',')}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleNotification(
            @Header(name = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(name = KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp,
            String event,
            Acknowledgment ack) {
        try {
            NotificationDto notification = objectMapper.readValue(event, NotificationDto.class);
            auditNotificationService.process(notification, topic, timestamp, event);
            ack.acknowledge();
            log.info("Audit Notification processed successfully: id={}, topic={}, timestamp={}",
                    notification.notificationId(), topic, timestamp);
        } catch (Exception e) {
            log.error("Failed to process message from topic [{}]: message=[{}], error={}", topic, event, e.getMessage());
            ack.acknowledge();
        }
    }
}

