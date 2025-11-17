package com.notification.core.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaValidationResponse {

    private Boolean allowed;
    private Long availableQuota;
    private Long requestedAmount;
    private String reason;
}

