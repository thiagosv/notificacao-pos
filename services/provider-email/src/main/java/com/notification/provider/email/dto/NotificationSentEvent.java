package com.notification.provider.email.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSentEvent {
    private String notificationId;
    private String providerId;
    private String providerMessageId;
    private LocalDateTime timestamp;
}

