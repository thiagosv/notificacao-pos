package com.notification.provider.audit.repository;

import com.notification.provider.audit.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.notificationId = :notificationId ORDER BY n.timestamp ASC, n.version ASC")
    List<Notification> findAllByNotificationIdOrderByTimestampAndVersion(@Param("notificationId") UUID notificationId);

}

