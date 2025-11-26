package com.notification.core.service;

import com.notification.core.dto.NotificationRequest;
import com.notification.core.dto.RenderTemplateResponse;
import com.notification.core.exception.QuotaServiceException;
import com.notification.core.exception.TemplateServiceException;
import com.notification.core.exception.TemplateServiceUnavailableException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final RestClient restClient;

    @Value("${template-service.url}")
    private String templateServiceUrl;

    @Retry(name = "templateService")
    public RenderTemplateResponse renderTemplate(NotificationRequest request) {
        try {
            log.debug("Calling Template Service to render template: {}", request.getTemplateCode());
            RenderTemplateResponse response = restClient.post()
                    .uri(templateServiceUrl + "/internal/v1/templates/render")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("Template render failed: status={}", res.getStatusCode());
                        throw new TemplateServiceException("Template service returned error: " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Template service error: status={}", res.getStatusCode());
                        throw new TemplateServiceUnavailableException("Template service returned error: " + res.getStatusCode());
                    })
                    .body(RenderTemplateResponse.class);

            log.debug("Template rendered successfully: {} version {}", response.getContent(), response.getTemplateVersion());
            return response;
        } catch (TemplateServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error renderizing template: {}", e.getMessage(), e);
            throw new TemplateServiceException("Failed to render template", e);
        }
    }
}

