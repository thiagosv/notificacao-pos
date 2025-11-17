package com.notification.core.service;

import com.notification.core.dto.NotificationRequest;
import com.notification.core.exception.NotificationNotFoundException;
import com.notification.core.model.Notification;
import com.notification.core.model.NotificationStatus;
import com.notification.core.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

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
     * Find notification by idempotency key
     */
    public Optional<Notification> findByIdempotencyKey(String idempotencyKey) {
        return notificationRepository.findByIdempotencyKey(idempotencyKey);
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
    public void updateStatusToSent(String notificationId, String providerId, String providerMessageId) {
        log.info("Updating notification to SENT: id={}, providerId={}", notificationId, providerId);

        Notification notification = findById(notificationId);
        notification.setStatus(NotificationStatus.SENT);
        notification.setProviderId(providerId);
        notification.setProviderMessageId(providerMessageId);
        notification.setSentAt(LocalDateTime.now());

        notificationRepository.save(notification);
        log.info("Notification status updated to SENT: id={}", notificationId);
    }

    /**
     * Handle notification failure
     */
    @Transactional
    public void handleFailure(String notificationId, String failureReason) {
        log.warn("Handling notification failure: id={}, reason={}", notificationId, failureReason);

        Notification notification = findById(notificationId);
        notification.setFailureReason(failureReason);
        notification.incrementRetryCount();

        if (notification.hasReachedMaxRetries()) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(LocalDateTime.now());
            log.error("Notification failed permanently: id={}, retries={}",
                     notificationId, notification.getRetryCount());
        } else {
            notification.setStatus(NotificationStatus.RETRYING);
            log.info("Notification will be retried: id={}, retryCount={}",
                    notificationId, notification.getRetryCount());
        }

        notificationRepository.save(notification);
    }

    /**
     * Update notification status to PROCESSING
     */
    @Transactional
    public void updateStatusToProcessing(String notificationId) {
        log.info("Updating notification to PROCESSING: id={}", notificationId);

        Notification notification = findById(notificationId);
        notification.setStatus(NotificationStatus.PROCESSING);

        notificationRepository.save(notification);
    }

    /**
     * Find notifications eligible for retry
     */
    public List<Notification> findEligibleForRetry(int minutesAgo) {
        LocalDateTime before = LocalDateTime.now().minusMinutes(minutesAgo);
        return notificationRepository.findEligibleForRetry(before);
    }

    /**
     * Get notification statistics
     */
    public NotificationStats getStats() {
        return NotificationStats.builder()
                .pending(notificationRepository.countByStatus(NotificationStatus.PENDING))
                .processing(notificationRepository.countByStatus(NotificationStatus.PROCESSING))
                .sent(notificationRepository.countByStatus(NotificationStatus.SENT))
                .failed(notificationRepository.countByStatus(NotificationStatus.FAILED))
                .retrying(notificationRepository.countByStatus(NotificationStatus.RETRYING))
                .build();
    }

    /**
     * Inner class for statistics
     */
    @lombok.Data
    @lombok.Builder
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

