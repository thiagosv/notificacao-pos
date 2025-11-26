package com.notification.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.notification.core.model.Channel;
import com.notification.core.model.Priority;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationRequest {
    
    @NotBlank(message = "Idempotency key is required")
    @Size(max = 255, message = "Idempotency key must be less than 255 characters")
    private String idempotencyKey;

    @NotBlank(message = "Client ID is required")
    @Size(max = 100, message = "Client ID must be less than 100 characters")
    private String clientId;

    @NotNull(message = "Channel is required")
    private Channel channel;

    @NotBlank(message = "Recipient is required")
    @Size(max = 255, message = "Recipient must be less than 255 characters")
    private String recipient;

    @NotBlank
    private String templateCode;

    @NotEmpty
    private Map<String, String> variables;

    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Min(value = 0, message = "Max retries must be at least 0")
    @Max(value = 10, message = "Max retries must be at most 10")
    @Builder.Default
    private Integer maxRetries = 3;
}

