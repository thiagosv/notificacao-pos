package com.notification.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationSentEvent {

    private String notificationId;
    private String providerId;
    private String providerMessageId;
    private BigDecimal cost;
    private LocalDateTime timestamp;
}

