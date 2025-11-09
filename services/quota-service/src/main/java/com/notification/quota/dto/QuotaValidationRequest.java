package com.notification.quota.dto;

import com.notification.quota.model.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaValidationRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotNull(message = "Channel is required")
    private Channel channel;

    @Positive(message = "Amount must be positive")
    private Long amount;

    private String notificationId;

}