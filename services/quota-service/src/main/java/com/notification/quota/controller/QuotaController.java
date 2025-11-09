package com.notification.quota.controller;

import com.notification.quota.dto.QuotaValidationRequest;
import com.notification.quota.dto.QuotaValidationResponse;
import com.notification.quota.dto.QuotaResponse;
import com.notification.quota.model.Channel;
import com.notification.quota.service.QuotaService;
import com.notification.quota.service.QuotaValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/quotas")
@RequiredArgsConstructor
@Tag(name = "Quota", description = "Quota management APIs")
public class QuotaController {

    private final QuotaService quotaService;
    private final QuotaValidationService quotaValidationService;

    @PostMapping("/validate")
    @Operation(summary = "Validate and consume quota", description = "Validates if quota is available and consumes it")
    public ResponseEntity<QuotaValidationResponse> validateAndConsume(
            @Valid @RequestBody QuotaValidationRequest request,
            @RequestHeader(value = "X-Client-Id", required = false) String clientIdHeader) {

        log.info("Quota validation request received: clientId={}, channel={}, amount={}",
                request.getClientId(), request.getChannel(), request.getAmount());

        // Se clientId vier do header (do Gateway), usar ele
        if (clientIdHeader != null && !clientIdHeader.isBlank())
            request.setClientId(clientIdHeader);

        QuotaValidationResponse response = quotaValidationService.validateAndConsume(request);

        return response.isAllowed()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @PostMapping("/check")
    @Operation(summary = "Check quota availability", description = "Checks if quota is available without consuming")
    public ResponseEntity<QuotaValidationResponse> checkQuota(@Valid @RequestBody QuotaValidationRequest request) {
        log.info("Quota check request received: clientId={}, channel={}, amount={}",
                request.getClientId(), request.getChannel(), request.getAmount());

        QuotaValidationResponse response = quotaValidationService.checkQuota(
                request.getClientId(),
                request.getChannel(),
                request.getAmount()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clientId}")
    @Operation(summary = "Get all quotas for a client")
    public ResponseEntity<List<QuotaResponse>> getClientQuotas(@PathVariable String clientId) {
        log.info("Getting quotas for clientId={}", clientId);

        List<QuotaResponse> quotas = quotaService.getAllQuotasByClient(clientId);
        return ResponseEntity.ok(quotas);
    }

    @GetMapping("/{clientId}/{channel}")
    @Operation(summary = "Get quota for specific client and channel")
    public ResponseEntity<QuotaResponse> getQuota(@PathVariable String clientId, @PathVariable Channel channel) {

        log.info("Getting quota for clientId={}, channel={}", clientId, channel);

        QuotaResponse quota = quotaService.getQuota(clientId, channel);
        return ResponseEntity.ok(quota);
    }

    @PostMapping("/release")
    @Operation(summary = "Release quota", description = "Releases consumed quota (e.g., due to failed notification)")
    public ResponseEntity<Void> releaseQuota(
            @RequestParam String clientId,
            @RequestParam Channel channel,
            @RequestParam Long amount,
            @RequestParam(required = false) String notificationId) {

        log.info("Releasing quota: clientId={}, channel={}, amount={}", clientId, channel, amount);

        quotaValidationService.releaseQuota(clientId, channel, amount, notificationId);
        return ResponseEntity.ok().build();
    }

}