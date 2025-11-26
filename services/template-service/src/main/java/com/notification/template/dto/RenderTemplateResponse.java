package com.notification.template.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenderTemplateResponse {

    private String content;
    private String subject;
    private UUID templateId;
    private Integer templateVersion;
}

