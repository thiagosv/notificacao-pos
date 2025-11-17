package com.notification.core.dto;

import com.notification.core.model.Channel;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaValidationRequest {

    private String clientId;
    private Channel channel;
    private Long amount;
    private String notificationId;
}

