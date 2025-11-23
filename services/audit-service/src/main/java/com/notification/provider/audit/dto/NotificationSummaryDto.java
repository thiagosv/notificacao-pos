package com.notification.provider.audit.dto;

import com.notification.provider.audit.model.NotificationStatus;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record NotificationSummaryDto(
        UUID notificationId,
        NotificationStatus currentStatus,
        List<TimelineEventDto> timeline,
        NotificationMetadataDto metadata
) {
}

