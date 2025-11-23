package com.notification.provider.audit.service;

import com.notification.provider.audit.dto.NotificationDto;
import com.notification.provider.audit.mapper.NotificationMapper;
import com.notification.provider.audit.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditNotificationService {

    private final NotificationRepository repository;

    public void process(NotificationDto event, String topic, Long timestamp, String message) {
        log.info("Processing notification from topic={}", topic);

        try {
            repository.save(NotificationMapper.of(event, topic, timestamp, message));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}

