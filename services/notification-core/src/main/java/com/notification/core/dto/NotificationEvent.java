package com.notification.core.dto;

import com.notification.core.model.Channel;
import com.notification.core.model.Notification;
import com.notification.core.model.Priority;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String notificationId;
    private String clientId;
    private Channel channel;
    private String recipient;
    private String subject;
    private String content;
    private Priority priority;
    private String idempotencyKey;
    private LocalDateTime timestamp;
    private TemplateMetadata template;

    public static NotificationEvent of(Notification notification) {
        return NotificationEvent.builder()
                .notificationId(notification.getId())
                .clientId(notification.getClientId())
                .channel(notification.getChannel())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .priority(notification.getPriority())
                .idempotencyKey(notification.getIdempotencyKey())
                .timestamp(LocalDateTime.now())
                .template(TemplateMetadata.builder()
                        .code(notification.getTemplateCode())
                        .id(notification.getTemplateId())
                        .version(notification.getTemplateVersion())
                        .build())
                .build();
    }

    @Getter
    @Setter
    @Builder
    public static class TemplateMetadata {
        private String code;
        private UUID id;
        private Integer version;
    }
}

