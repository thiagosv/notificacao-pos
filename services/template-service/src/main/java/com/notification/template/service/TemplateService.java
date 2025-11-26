package com.notification.template.service;

import com.notification.template.dto.*;
import com.notification.template.exception.MissingVariablesException;
import com.notification.template.exception.TemplateNotFoundException;
import com.notification.template.metrics.MetricsService;
import com.notification.template.model.Channel;
import com.notification.template.model.Template;
import com.notification.template.repository.TemplateRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateCacheService cacheService;
    private final TemplateRenderService renderService;
    private final MetricsService metricsService;

    @Transactional
    public TemplateResponse createTemplate(String clientId, CreateTemplateRequest request) {
        log.info("Creating template for client: {}, code: {}, channel: {}",
                clientId, request.getTemplateCode(), request.getChannel());

        Optional<Template> existing = templateRepository.findByClientIdAndChannelAndTemplateCodeAndActiveTrue(
                clientId, request.getChannel(), request.getTemplateCode()
        );

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    String.format("Template '%s' already exists for client '%s' and channel '%s'",
                            request.getTemplateCode(), clientId, request.getChannel())
            );
        }

        Template template = Template.builder()
                .clientId(clientId)
                .channel(request.getChannel())
                .templateCode(request.getTemplateCode())
                .content(request.getContent())
                .subject(request.getSubject())
                .variables(new HashSet<>(request.getVariables()))
                .version(1)
                .active(true)
                .build();

        template = templateRepository.save(template);

        cacheService.put(clientId, request.getChannel(), request.getTemplateCode(), template);

        metricsService.templateCreated(clientId, request.getChannel());

        log.info("Template created successfully: {}", template.getId());
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplate(String clientId, String templateCode, Channel channel) {
        Template template = findActiveTemplate(clientId, channel, templateCode);
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public TemplateListResponse listTemplates(String clientId, Channel channel) {
        List<Template> templates;

        if (channel != null) {
            templates = templateRepository.findByClientIdAndChannelAndActiveTrue(clientId, channel);
        } else {
            templates = templateRepository.findByClientIdAndActiveTrue(clientId);
        }

        List<TemplateListResponse.TemplateSummary> summaries = templates.stream()
                .map(t -> TemplateListResponse.TemplateSummary.builder()
                        .templateCode(t.getTemplateCode())
                        .channel(t.getChannel().name())
                        .version(t.getVersion())
                        .active(t.getActive())
                        .createdAt(t.getCreatedAt().toString())
                        .build())
                .toList();

        return TemplateListResponse.builder()
                .templates(summaries)
                .total(summaries.size())
                .build();
    }

    @Transactional
    public TemplateResponse updateTemplate(String clientId, String templateCode, Channel channel, UpdateTemplateRequest request) {
        log.info("Updating template for client: {}, code: {}, channel: {}",
                clientId, templateCode, channel);

        Template currentTemplate = findActiveTemplate(clientId, channel, templateCode);

        currentTemplate.setActive(false);
        templateRepository.save(currentTemplate);

        Integer newVersion = currentTemplate.getVersion() + 1;

        Template newTemplate = Template.builder()
                .clientId(clientId)
                .channel(channel)
                .templateCode(templateCode)
                .content(request.getContent())
                .subject(request.getSubject())
                .variables(new HashSet<>(request.getVariables()))
                .version(newVersion)
                .active(true)
                .build();

        newTemplate = templateRepository.save(newTemplate);

        cacheService.evict(clientId, channel, templateCode);
        cacheService.put(clientId, channel, templateCode, newTemplate);

        log.info("Template updated successfully: {} -> version {}", newTemplate.getId(), newVersion);
        return toResponse(newTemplate);
    }

    @Transactional
    public void deleteTemplate(String clientId, String templateCode, Channel channel) {
        log.info("Deleting template for client: {}, code: {}, channel: {}",
                clientId, templateCode, channel);

        Template template = findActiveTemplate(clientId, channel, templateCode);
        template.setActive(false);
        templateRepository.save(template);

        cacheService.evict(clientId, channel, templateCode);

        log.info("Template deleted successfully: {}", template.getId());
    }

    @Transactional(readOnly = true)
    public RenderTemplateResponse renderTemplate(RenderTemplateRequest request) {
        Timer.Sample timerSample = metricsService.getRenderTimer();

        try {
            log.info("Rendering template for client: {}, code: {}, channel: {}",
                    request.getClientId(), request.getTemplateCode(), request.getChannel());

            Template template = findActiveTemplate(
                    request.getClientId(),
                    request.getChannel(),
                    request.getTemplateCode()
            );

            validateVariables(template.getVariables(), request.getVariables().keySet());

            String renderedContent = renderService.render(template.getContent(), request.getVariables());
            String renderedSubject = template.getSubject() != null
                    ? renderService.render(template.getSubject(), request.getVariables())
                    : null;

            metricsService.renderMetrics(request, timerSample, true);
            log.info("Template rendered successfully");

            return RenderTemplateResponse.builder()
                    .content(renderedContent)
                    .subject(renderedSubject)
                    .templateId(template.getId())
                    .templateVersion(template.getVersion())
                    .build();

        } catch (Exception e) {
            metricsService.renderMetrics(request, timerSample, false);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplateHistory(String clientId, String templateCode, Channel channel) {
        List<Template> templates = templateRepository
                .findByClientIdAndChannelAndTemplateCodeOrderByVersionDesc(clientId, channel, templateCode);

        return templates.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RenderTemplateResponse previewTemplate(String clientId, String templateCode,
                                                  Channel channel, RenderTemplateRequest request) {
        Template template = findActiveTemplate(clientId, channel, templateCode);

        validateVariables(template.getVariables(), request.getVariables().keySet());

        String renderedContent = renderService.render(template.getContent(), request.getVariables());
        String renderedSubject = template.getSubject() != null
                ? renderService.render(template.getSubject(), request.getVariables())
                : null;

        return RenderTemplateResponse.builder()
                .content(renderedContent)
                .subject(renderedSubject)
                .templateId(template.getId())
                .templateVersion(template.getVersion())
                .build();
    }

    private Template findActiveTemplate(String clientId, Channel channel, String templateCode) {
        Optional<Template> cached = cacheService.get(clientId, channel, templateCode);
        if (cached.isPresent()) {
            metricsService.templateCache();
            return cached.get();
        }

        metricsService.templateMiss();

        Template template = templateRepository
                .findByClientIdAndChannelAndTemplateCodeAndActiveTrue(clientId, channel, templateCode)
                .orElseThrow(() -> new TemplateNotFoundException(
                        String.format("Template '%s' not found for client '%s' and channel '%s'",
                                templateCode, clientId, channel)
                ));

        cacheService.put(clientId, channel, templateCode, template);

        return template;
    }

    private void validateVariables(Set<String> required, Set<String> provided) {
        Set<String> missing = new HashSet<>(required);
        missing.removeAll(provided);

        if (!missing.isEmpty()) {
            throw new MissingVariablesException(required, provided);
        }
    }

    private TemplateResponse toResponse(Template template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .clientId(template.getClientId())
                .channel(template.getChannel())
                .templateCode(template.getTemplateCode())
                .version(template.getVersion())
                .content(template.getContent())
                .subject(template.getSubject())
                .variables(template.getVariables())
                .active(template.getActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}

