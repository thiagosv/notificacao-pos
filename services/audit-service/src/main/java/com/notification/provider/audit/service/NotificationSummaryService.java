package com.notification.provider.audit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.provider.audit.dto.NotificationMetadataDto;
import com.notification.provider.audit.dto.NotificationSummaryDto;
import com.notification.provider.audit.dto.TimelineEventDto;
import com.notification.provider.audit.model.Notification;
import com.notification.provider.audit.model.NotificationStatus;
import com.notification.provider.audit.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSummaryService {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationSummaryDto getNotificationSummary(UUID notificationId) {
        List<Notification> events = notificationRepository
                .findAllByNotificationIdOrderByTimestampAndVersion(notificationId);

        if (events.isEmpty())
            return null;

        List<TimelineEventDto> timeline = events.stream()
                .map(this::buildTimelineEvent)
                .toList();

        NotificationStatus currentStatus = events.getLast().getNotificationStatus();
        NotificationMetadataDto metadata = buildMetadata(events);

        return NotificationSummaryDto.builder()
                .notificationId(notificationId)
                .currentStatus(currentStatus)
                .timeline(timeline)
                .metadata(metadata)
                .build();
    }

    private TimelineEventDto buildTimelineEvent(Notification notification) {
        try {
            JsonNode payloadNode = objectMapper.readTree(notification.getPayload());

            String provider = payloadNode.has("provider") ? payloadNode.get("provider").asText() : null;
            Double cost = payloadNode.has("cost") ? payloadNode.get("cost").asDouble() : null;

            return TimelineEventDto.builder()
                    .event(notification.getNotificationStatus())
                    .timestamp(notification.getTimestamp())
                    .version(notification.getVersion())
                    .provider(provider)
                    .cost(cost)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing payload for notification {}: {}", notification.getId(), e.getMessage());
            return TimelineEventDto.builder()
                    .event(notification.getNotificationStatus())
                    .timestamp(notification.getTimestamp())
                    .version(notification.getVersion())
                    .build();
        }
    }

    private NotificationMetadataDto buildMetadata(List<Notification> events) {
        int totalEvents = events.size();

        long totalAttempts = events.stream()
                .filter(e ->
                        NotificationStatus.RETRYING == e.getNotificationStatus()
                                || NotificationStatus.SENT == e.getNotificationStatus()
                ).count();

        double totalCost = events.stream()
                .mapToDouble(this::extractCostFromPayload)
                .sum();

        return NotificationMetadataDto.builder()
                .totalEvents(totalEvents)
                .totalAttempts((int) totalAttempts)
                .totalCost(totalCost)
                .build();
    }

    private double extractCostFromPayload(Notification notification) {
        try {
            JsonNode payloadNode = objectMapper.readTree(notification.getPayload());
            if (payloadNode.has("cost"))
                return payloadNode.get("cost").asDouble();
        } catch (Exception e) {
            log.debug("Could not extract cost from payload for notification {}", notification.getId());
        }
        return 0.0;
    }
}

