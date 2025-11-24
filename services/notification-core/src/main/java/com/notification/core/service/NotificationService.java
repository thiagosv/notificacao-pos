package com.notification.core.service;

import com.notification.core.dto.NotificationRequest;
import com.notification.core.dto.NotificationSentEvent;
import com.notification.core.exception.NotificationNotFoundException;
import com.notification.core.metrics.MetricsService;
import com.notification.core.model.Notification;
import com.notification.core.model.NotificationStatus;
import com.notification.core.repository.NotificationRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MetricsService metricsService;

    /**
     * Create a new notification
     */
    @Transactional
    public Notification createNotification(NotificationRequest request) {
        log.info("Creating notification: clientId={}, channel={}, idempotencyKey={}",
                request.getClientId(), request.getChannel(), request.getIdempotencyKey());

        Notification notification = Notification.builder()
                .clientId(request.getClientId())
                .idempotencyKey(request.getIdempotencyKey())
                .channel(request.getChannel())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(NotificationStatus.PENDING)
                .priority(request.getPriority())
                .retryCount(0)
                .maxRetries(request.getMaxRetries())
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created: id={}", notification.getId());

        metricsService.incrementPending(notification.getChannel());
        return notification;
    }

    /**
     * Find notification by ID
     */
    public Notification findById(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + id));
    }

    /**
     * Find notifications by client ID
     */
    public Page<Notification> findByClientId(String clientId, Pageable pageable) {
        return notificationRepository.findByClientId(clientId, pageable);
    }

    /**
     * Update notification status to SENT
     */
    @Transactional
    public void updateStatusToSent(NotificationSentEvent event) {
        log.info("Updating notification to SENT: id={}, providerId={}", event.getNotificationId(), event.getProviderId());

        Notification notification = findById(event.getNotificationId());
        notification.setStatus(NotificationStatus.SENT);
        notification.setProviderId(event.getProviderId());
        notification.setProviderMessageId(event.getProviderMessageId());
        notification.setSentAt(LocalDateTime.now());

        notificationRepository.save(notification);
        log.info("Notification status updated to SENT: id={}", event.getNotificationId());
        metricsService.incrementSent(notification, event);
    }

    /**
     * Handle notification failure
     * Returns the updated notification so caller can decide to republish if needed
     */
    @Transactional
    public Notification handleFailure(String notificationId, String failureReason) {
        log.warn("Handling notification failure: id={}, reason={}", notificationId, failureReason);

        Notification notification = findById(notificationId);
        notification.setFailureReason(failureReason);
        notification.incrementRetryCount();

        if (notification.hasReachedMaxRetries()) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(LocalDateTime.now());
            log.error("Notification failed permanently: id={}, retries={}", notificationId, notification.getRetryCount());

            metricsService.incrementFailed(notification.getChannel());
        } else {
            notification.setStatus(NotificationStatus.RETRYING);
            log.info("Notification will be retried: id={}, retryCount={}", notificationId, notification.getRetryCount());

            metricsService.incrementRetry(notification.getChannel(), String.valueOf(notification.getRetryCount()));
        }

        return notificationRepository.save(notification);
    }

    public NotificationStats getStats() {
        return NotificationStats.builder()
                .pending(notificationRepository.countByStatus(NotificationStatus.PENDING))
                .processing(notificationRepository.countByStatus(NotificationStatus.PROCESSING))
                .sent(notificationRepository.countByStatus(NotificationStatus.SENT))
                .failed(notificationRepository.countByStatus(NotificationStatus.FAILED))
                .retrying(notificationRepository.countByStatus(NotificationStatus.RETRYING))
                .build();
    }

    @Data
    @Builder
    public static class NotificationStats {
        private long pending;
        private long processing;
        private long sent;
        private long failed;
        private long retrying;

        public long getTotal() {
            return pending + processing + sent + failed + retrying;
        }
    }
}

