package com.notification.provider.audit.dto;

import lombok.Builder;

@Builder
public record NotificationMetadataDto(
        Integer totalEvents,
        Integer totalAttempts,
        Double totalCost
) {
}

