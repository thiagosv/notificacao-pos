package com.notification.provider.push.dto;

import lombok.*;

import java.math.BigDecimal;
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
    private BigDecimal cost;
    private LocalDateTime timestamp;
}

