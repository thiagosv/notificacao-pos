package com.notification.provider.sms.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String notificationId;
    private String clientId;
    private String channel;
    private String recipient;
    private String subject;
    private String content;
    private String priority;
    private String idempotencyKey;
    private LocalDateTime timestamp;
}

