package com.notification.provider.audit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.notification.provider.audit.model.NotificationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimelineEventDto(
        NotificationStatus event,
        LocalDateTime timestamp,
        Integer version,
        String provider,
        Double cost
) {
}

