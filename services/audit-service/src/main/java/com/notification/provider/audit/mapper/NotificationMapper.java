package com.notification.provider.audit.mapper;

import com.notification.provider.audit.dto.NotificationDto;
import com.notification.provider.audit.model.Notification;
import com.notification.provider.audit.model.NotificationStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public interface NotificationMapper {

    static Notification of(NotificationDto notificationDto, String topic, Long timestamp, String message) {
        return Notification.builder()
                .notificationId(UUID.fromString(notificationDto.notificationId()))
                .notificationStatus(statusFromTopic(topic))
                .payload(message)
                .timestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    static NotificationStatus statusFromTopic(String topic) {
        return switch (topic) {
            case "notification.created" -> NotificationStatus.CREATED;
            case "notification.sent" -> NotificationStatus.SENT;
            case "notification.failed" -> NotificationStatus.RETRYING;
            case "notification.dlq" -> NotificationStatus.FAILED;
            default -> throw new IllegalArgumentException("Unknown topic: " + topic);
        };
    }
}
