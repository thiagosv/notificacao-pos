package com.notification.provider.sms.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {
    private String number;
    private String title;
    private String body;
    private String priority;
}

