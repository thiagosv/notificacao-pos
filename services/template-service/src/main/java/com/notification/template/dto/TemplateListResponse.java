package com.notification.template.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateListResponse {

    private List<TemplateSummary> templates;
    private long total;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateSummary {
        private String templateCode;
        private String channel;
        private Integer version;
        private Boolean active;
        private String createdAt;
    }
}

