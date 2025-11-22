package com.notification.provider.sms.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFailedEvent {
    private String notificationId;
    private String failureReason;
    private LocalDateTime timestamp;
}

