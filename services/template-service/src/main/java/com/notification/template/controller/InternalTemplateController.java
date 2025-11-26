package com.notification.template.controller;

import com.notification.template.dto.RenderTemplateRequest;
import com.notification.template.dto.RenderTemplateResponse;
import com.notification.template.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/v1/templates")
@RequiredArgsConstructor
public class InternalTemplateController {

    private final TemplateService templateService;

    @PostMapping("/render")
    public ResponseEntity<RenderTemplateResponse> renderTemplate(@Valid @RequestBody RenderTemplateRequest request) {

        log.info("POST /internal/v1/templates/render - clientId: {}, templateCode: {}, channel: {}",
                request.getClientId(), request.getTemplateCode(), request.getChannel());

        RenderTemplateResponse response = templateService.renderTemplate(request);
        return ResponseEntity.ok(response);
    }
}

