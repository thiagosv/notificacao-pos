package com.notification.provider.push.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushResponse {
    private boolean success;
    private String messageId;
    private String error;
    private String message;
    private String timestamp;
}

