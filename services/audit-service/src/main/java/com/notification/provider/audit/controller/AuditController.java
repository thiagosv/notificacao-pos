package com.notification.provider.audit.controller;

import com.notification.provider.audit.dto.NotificationSummaryDto;
import com.notification.provider.audit.service.NotificationSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "Endpoints para visualização de auditoria")
public class AuditController {

    private final NotificationSummaryService service;

    @GetMapping("/notifications/{notificationId}")
    @Operation(summary = "Obter dados de auditoria de um ID especifico",
            description = "Retorna os eventos processados da notificacao")
    public ResponseEntity<NotificationSummaryDto> getNotificationEvents(@PathVariable String notificationId) {
        return ResponseEntity.ok(service.getNotificationSummary(UUID.fromString(notificationId)));
    }

}
