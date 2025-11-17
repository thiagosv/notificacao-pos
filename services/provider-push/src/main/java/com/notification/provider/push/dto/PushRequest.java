package com.notification.provider.push.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushRequest {
    private String deviceToken;
    private String title;
    private String body;
    private String priority;
}

