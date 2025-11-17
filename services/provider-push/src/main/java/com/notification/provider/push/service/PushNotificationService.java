package com.notification.provider.push.service;

import com.notification.provider.push.client.PushProviderClient;
import com.notification.provider.push.dto.NotificationEvent;
import com.notification.provider.push.dto.PushRequest;
import com.notification.provider.push.dto.PushResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushProviderClient pushProviderClient;

    public PushResponse sendPushNotification(NotificationEvent event) {
        log.info("Processing PUSH notification: id={}", event.getNotificationId());

        PushRequest request = PushRequest.builder()
            .deviceToken(event.getRecipient())
            .title(event.getSubject())
            .body(event.getContent())
            .priority(event.getPriority())
            .build();

        try {
            return pushProviderClient.send(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send push notification", e);
        }
    }
}

