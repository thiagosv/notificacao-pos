package com.notification.provider.sms.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SmsResponse {
    private boolean success;
    private String provider;
    private String messageId;
    private String error;
    private String message;
    private BigDecimal cost;
    private String timestamp;
}

