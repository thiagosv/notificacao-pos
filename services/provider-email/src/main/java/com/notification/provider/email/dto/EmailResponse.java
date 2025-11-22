package com.notification.provider.email.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailResponse {
    private boolean success;
    private String messageId;
    private String error;
    private String message;
    private String timestamp;
}

