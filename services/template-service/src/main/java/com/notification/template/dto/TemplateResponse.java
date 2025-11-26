package com.notification.template.dto;

import com.notification.template.model.Channel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {

    private UUID id;
    private String clientId;
    private Channel channel;
    private String templateCode;
    private Integer version;
    private String content;
    private String subject;
    private Set<String> variables;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

