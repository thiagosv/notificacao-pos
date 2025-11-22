package com.notification.core.controller;

import com.notification.core.metrics.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller para visualização de métricas
 */
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Endpoints para visualização de métricas")
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/notifications")
    @Operation(summary = "Obter contadores de notificações",
               description = "Retorna os contadores atuais de notificações por canal e status")
    public ResponseEntity<Map<String, Double>> getNotificationCounts() {
        return ResponseEntity.ok(metricsService.getCurrentCounts());
    }
}

