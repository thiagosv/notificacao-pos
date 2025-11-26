package com.notification.template.dto;

import com.notification.template.model.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateRequest {

    @NotBlank(message = "Template code is required")
    private String templateCode;

    @NotNull(message = "Channel is required")
    private Channel channel;

    @NotBlank(message = "Content is required")
    private String content;

    private String subject;

    @NotEmpty(message = "At least one variable is required")
    private Set<String> variables;
}

