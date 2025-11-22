package com.notification.provider.sms.service;

import com.notification.provider.sms.dto.NotificationEvent;
import com.notification.provider.sms.dto.SmsRequest;
import com.notification.provider.sms.dto.SmsResponse;
import com.notification.provider.sms.provider.SmsProviderChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotificationService {

    private final SmsProviderChain smsProviderChain;

    public SmsResponse sendSmsNotification(NotificationEvent event) {
        log.info("Processing SMS notification: id={}", event.getNotificationId());

        SmsRequest request = SmsRequest.builder()
            .number(event.getRecipient())
            .title(event.getSubject())
            .body(event.getContent())
            .priority(event.getPriority())
            .build();

        try {
            return smsProviderChain.send(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send sms notification", e);
        }
    }
}

