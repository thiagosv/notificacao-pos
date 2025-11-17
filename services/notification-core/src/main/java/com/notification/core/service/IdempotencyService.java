package com.notification.core.service;

import com.notification.core.model.Notification;
import com.notification.core.repository.IdempotencyRepository;
import com.notification.core.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final NotificationRepository notificationRepository;

    @Value("${idempotency.ttl:86400}")
    private long ttl;

    public Optional<Notification> findByIdempotencyKey(String idempotencyKey) {
        log.debug("Checking idempotency key: {}", idempotencyKey);

        Optional<String> cachedNotificationId = idempotencyRepository.findNotificationId(idempotencyKey);
        if (cachedNotificationId.isPresent()) {
            log.debug("Found in Redis cache: {} -> {}", idempotencyKey, cachedNotificationId.get());
            return notificationRepository.findById(cachedNotificationId.get());
        }

        Optional<Notification> notification = notificationRepository.findByIdempotencyKey(idempotencyKey);
        if (notification.isPresent()) {
            log.debug("Found in database: {} -> {}", idempotencyKey, notification.get().getId());
            register(idempotencyKey, notification.get().getId());
        }

        return notification;
    }

    public void register(String idempotencyKey, String notificationId) {
        log.debug("Registering idempotency: {} -> {}", idempotencyKey, notificationId);
        idempotencyRepository.register(idempotencyKey, notificationId, ttl);
    }
}

