package com.notification.template.controller;

import com.notification.template.dto.*;
import com.notification.template.model.Channel;
import com.notification.template.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @RequestHeader("X-Client-ID") String clientId,
            @Valid @RequestBody CreateTemplateRequest request) {

        log.info("POST /templates - clientId: {}, templateCode: {}", clientId, request.getTemplateCode());

        TemplateResponse response = templateService.createTemplate(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<TemplateListResponse> listTemplates(
            @RequestHeader("X-Client-ID") String clientId,
            @RequestParam(required = false) Channel channel) {

        log.info("GET /templates - clientId: {}, channel: {}", clientId, channel);

        TemplateListResponse response = templateService.listTemplates(clientId, channel);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{templateCode}")
    public ResponseEntity<TemplateResponse> getTemplate(
            @RequestHeader("X-Client-ID") String clientId,
            @PathVariable String templateCode,
            @RequestParam Channel channel) {

        log.info("GET /templates/{} - clientId: {}, channel: {}", templateCode, clientId, channel);

        TemplateResponse response = templateService.getTemplate(clientId, templateCode, channel);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{templateCode}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @RequestHeader("X-Client-ID") String clientId,
            @PathVariable String templateCode,
            @RequestParam Channel channel,
            @Valid @RequestBody UpdateTemplateRequest request) {

        log.info("PUT /templates/{} - clientId: {}, channel: {}", templateCode, clientId, channel);

        TemplateResponse response = templateService.updateTemplate(clientId, templateCode, channel, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{templateCode}")
    public ResponseEntity<Void> deleteTemplate(
            @RequestHeader("X-Client-ID") String clientId,
            @PathVariable String templateCode,
            @RequestParam Channel channel) {

        log.info("DELETE /templates/{} - clientId: {}, channel: {}", templateCode, clientId, channel);

        templateService.deleteTemplate(clientId, templateCode, channel);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{templateCode}/history")
    public ResponseEntity<List<TemplateResponse>> getTemplateHistory(
            @RequestHeader("X-Client-ID") String clientId,
            @PathVariable String templateCode,
            @RequestParam Channel channel) {

        log.info("GET /templates/{}/history - clientId: {}, channel: {}",
                templateCode, clientId, channel);

        List<TemplateResponse> response = templateService.getTemplateHistory(clientId, templateCode, channel);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{templateCode}/preview")
    public ResponseEntity<RenderTemplateResponse> previewTemplate(
            @RequestHeader("X-Client-ID") String clientId,
            @PathVariable String templateCode,
            @RequestParam Channel channel,
            @Valid @RequestBody RenderTemplateRequest request) {

        log.info("POST /templates/{}/preview - clientId: {}, channel: {}", templateCode, clientId, channel);

        RenderTemplateResponse response = templateService.previewTemplate(clientId, templateCode, channel, request);
        return ResponseEntity.ok(response);
    }
}

