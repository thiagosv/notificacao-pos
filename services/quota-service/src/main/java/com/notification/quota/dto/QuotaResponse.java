package com.notification.quota.dto;

import com.notification.quota.model.Channel;
import com.notification.quota.model.Quota;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaResponse {

    private Long id;
    private String clientId;
    private Channel channel;
    private Long totalQuota;
    private Long usedQuota;
    private Long availableQuota;
    private Double usagePercentage;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuotaResponse fromEntity(Quota quota) {
        double usagePercentage = quota.getTotalQuota() > 0
                ? (quota.getUsedQuota() * 100.0) / quota.getTotalQuota()
                : 0.0;

        return QuotaResponse.builder()
                .id(quota.getId())
                .clientId(quota.getClientId())
                .channel(quota.getChannel())
                .totalQuota(quota.getTotalQuota())
                .usedQuota(quota.getUsedQuota())
                .availableQuota(quota.getAvailableQuota())
                .usagePercentage(Math.round(usagePercentage * 100.0) / 100.0)
                .active(quota.getActive())
                .createdAt(quota.getCreatedAt())
                .updatedAt(quota.getUpdatedAt())
                .build();
    }

}