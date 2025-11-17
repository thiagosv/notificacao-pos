package com.notification.core.controller;

import com.notification.core.dto.NotificationRequest;
import com.notification.core.dto.NotificationResponse;
import com.notification.core.model.Notification;
import com.notification.core.service.NotificationOrchestrator;
import com.notification.core.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management API")
public class NotificationController {

    private final NotificationOrchestrator notificationOrchestrator;
    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "Send notification", description = "Create and send a notification through specified channel")
    @ApiResponse(responseCode = "201", description = "Notification created successfully")
    @ApiResponse(responseCode = "409", description = "Duplicate notification (idempotency key already exists)")
    @ApiResponse(responseCode = "429", description = "Quota exceeded")
    @ApiResponse(responseCode = "503", description = "Quota service unavailable")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Received notification request: idempotencyKey={}, channel={}", request.getIdempotencyKey(), request.getChannel());

        NotificationResponse response = notificationOrchestrator.sendNotification(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve notification details by ID")
    @ApiResponse(responseCode = "200", description = "Notification found")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable String id) {
        log.info("Fetching notification: id={}", id);

        Notification notification = notificationService.findById(id);
        NotificationResponse response = NotificationResponse.of(notification);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "List notifications by client", description = "Get paginated list of notifications for a client")
    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    public ResponseEntity<Page<NotificationResponse>> getClientNotifications(
            @PathVariable String clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("Fetching notifications for client: clientId={}, page={}, size={}", clientId, page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Notification> notifications = notificationService.findByClientId(clientId, pageable);
        Page<NotificationResponse> response = notifications.map(NotificationResponse::of);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get notification statistics", description = "Retrieve notification statistics by status")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<NotificationService.NotificationStats> getStats() {
        log.info("Fetching notification statistics");

        NotificationService.NotificationStats stats = notificationService.getStats();

        return ResponseEntity.ok(stats);
    }
}

