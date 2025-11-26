package com.notification.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTemplateRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private String subject;

    @NotEmpty(message = "At least one variable is required")
    private Set<String> variables;
}

