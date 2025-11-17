package com.notification.core.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFailedEvent {

    private String notificationId;
    private String providerId;
    private String failureReason;
    private Boolean retryable;
    private LocalDateTime failedAt;
}

