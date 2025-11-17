package com.notification.core.dto;

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
    private LocalDateTime sentAt;
}

