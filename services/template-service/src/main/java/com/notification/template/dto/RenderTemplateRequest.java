package com.notification.template.dto;

import com.notification.template.model.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenderTemplateRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotNull(message = "Channel is required")
    private Channel channel;

    @NotBlank(message = "Template code is required")
    private String templateCode;

    @NotEmpty(message = "Variables are required")
    private Map<String, String> variables;
}

