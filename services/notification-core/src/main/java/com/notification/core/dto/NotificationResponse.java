package com.notification.core.dto;

import com.notification.core.model.Channel;
import com.notification.core.model.Notification;
import com.notification.core.model.NotificationStatus;
import com.notification.core.model.Priority;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String notificationId;
    private String idempotencyKey;
    private String clientId;
    private Channel channel;
    private String recipient;
    private String subject;
    private String templateCode;
    private UUID templateId;
    private Integer templateVersion;
    private NotificationStatus status;
    private Priority priority;
    private String message;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationResponse of(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .idempotencyKey(notification.getIdempotencyKey())
                .clientId(notification.getClientId())
                .channel(notification.getChannel())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .templateCode(notification.getTemplateCode())
                .templateId(notification.getTemplateId())
                .templateVersion(notification.getTemplateVersion())
                .status(notification.getStatus())
                .priority(notification.getPriority())
                .message("Notification created successfully")
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}

