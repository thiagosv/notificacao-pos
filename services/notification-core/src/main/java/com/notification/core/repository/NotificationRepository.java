package com.notification.core.repository;

import com.notification.core.model.Channel;
import com.notification.core.model.Notification;
import com.notification.core.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /**
     * Find notification by idempotency key
     */
    Optional<Notification> findByIdempotencyKey(String idempotencyKey);

    /**
     * Check if idempotency key exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Find all notifications by client ID
     */
    Page<Notification> findByClientId(String clientId, Pageable pageable);

    /**
     * Find notifications by client ID and channel
     */
    Page<Notification> findByClientIdAndChannel(String clientId, Channel channel, Pageable pageable);

    /**
     * Find notifications by status
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Find notifications eligible for retry
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'RETRYING' " +
           "AND n.retryCount < n.maxRetries " +
           "AND n.updatedAt < :before")
    List<Notification> findEligibleForRetry(@Param("before") LocalDateTime before);

    /**
     * Count notifications by status
     */
    long countByStatus(NotificationStatus status);

    /**
     * Count notifications by client and channel
     */
    long countByClientIdAndChannel(String clientId, Channel channel);

    /**
     * Find recent notifications (last 24h)
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("since") LocalDateTime since);
}

