package com.notification.provider.email.service;

import com.notification.provider.email.dto.NotificationEvent;
import com.notification.provider.email.dto.EmailRequest;
import com.notification.provider.email.dto.EmailResponse;
import com.notification.provider.email.provider.EmailProviderChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final EmailProviderChain emailProviderChain;

    public EmailResponse sendEmailNotification(NotificationEvent event) {
        log.info("Processing EMAIL notification: id={}", event.getNotificationId());

        EmailRequest request = EmailRequest.builder()
                .email(event.getRecipient())
                .title(event.getSubject())
                .body(event.getContent())
                .priority(event.getPriority())
                .build();

        try {
            return emailProviderChain.send(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}

