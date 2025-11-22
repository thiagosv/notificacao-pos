package com.notification.provider.sms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsResponse {
    private boolean success;
    private String messageId;
    private String error;
    private String message;
    private String timestamp;
}

