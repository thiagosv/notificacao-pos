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
public class QuotaCreateRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotNull(message = "Channel is required")
    private Channel channel;

    @NotNull(message = "Total quota is required")
    @Positive(message = "Total quota must be positive")
    private Long totalQuota;

    @Builder.Default
    private Boolean active = true;

}