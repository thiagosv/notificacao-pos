package com.notification.quota.controller;

import com.notification.quota.dto.QuotaCreateRequest;
import com.notification.quota.dto.QuotaResponse;
import com.notification.quota.model.Channel;
import com.notification.quota.service.QuotaService;
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
@RequestMapping("/admin/quotas")
@RequiredArgsConstructor
@Tag(name = "Quota Admin", description = "Administrative quota management APIs")
public class QuotaAdminController {

    private final QuotaService quotaService;

    @PostMapping
    @Operation(summary = "Create new quota")
    public ResponseEntity<QuotaResponse> createQuota(@Valid @RequestBody QuotaCreateRequest request) {
        log.info("Creating quota for clientId={}, channel={}", request.getClientId(), request.getChannel());

        QuotaResponse quota = quotaService.createQuota(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(quota);
    }

    @PutMapping("/{clientId}/{channel}")
    @Operation(summary = "Update quota limit")
    public ResponseEntity<QuotaResponse> updateQuotaLimit(
            @PathVariable String clientId,
            @PathVariable Channel channel,
            @RequestParam Long newTotalQuota) {

        log.info("Updating quota limit for clientId={}, channel={}, newTotal={}",
                clientId, channel, newTotalQuota);

        QuotaResponse quota = quotaService.updateQuotaLimit(clientId, channel, newTotalQuota);
        return ResponseEntity.ok(quota);
    }

    @PostMapping("/{clientId}/{channel}/reset")
    @Operation(summary = "Reset quota usage")
    public ResponseEntity<QuotaResponse> resetQuota(@PathVariable String clientId, @PathVariable Channel channel) {

        log.info("Resetting quota for clientId={}, channel={}", clientId, channel);

        QuotaResponse quota = quotaService.resetQuota(clientId, channel);
        return ResponseEntity.ok(quota);
    }

    @DeleteMapping("/{clientId}/{channel}")
    @Operation(summary = "Delete quota")
    public ResponseEntity<Void> deleteQuota(@PathVariable String clientId, @PathVariable Channel channel) {

        log.info("Deleting quota for clientId={}, channel={}", clientId, channel);

        quotaService.deleteQuota(clientId, channel);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/near-limit")
    @Operation(summary = "Get quotas near limit")
    public ResponseEntity<List<QuotaResponse>> getQuotasNearLimit(@RequestParam(defaultValue = "100") Long threshold) {

        log.info("Getting quotas near limit with threshold={}", threshold);

        List<QuotaResponse> quotas = quotaService.getQuotasNearLimit(threshold);
        return ResponseEntity.ok(quotas);
    }

}