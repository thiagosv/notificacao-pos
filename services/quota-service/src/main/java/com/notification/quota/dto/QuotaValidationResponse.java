package com.notification.quota.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaValidationResponse {

    private boolean allowed;
    private String message;
    private Long availableQuota;
    private Long requestedAmount;
    private String reason;

    public static QuotaValidationResponse allowed(Long availableQuota, Long requestedAmount) {
        return QuotaValidationResponse.builder()
                .allowed(true)
                .message("Quota validation successful")
                .availableQuota(availableQuota)
                .requestedAmount(requestedAmount)
                .build();
    }

    public static QuotaValidationResponse denied(String reason, Long availableQuota, Long requestedAmount) {
        return QuotaValidationResponse.builder()
                .allowed(false)
                .message("Quota validation failed")
                .availableQuota(availableQuota)
                .requestedAmount(requestedAmount)
                .reason(reason)
                .build();
    }

}