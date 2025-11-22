package com.notification.provider.email.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String email;
    private String title;
    private String body;
    private String priority;
}

