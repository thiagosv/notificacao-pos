package com.notification.quota.service;

import com.notification.quota.dto.QuotaCreateRequest;
import com.notification.quota.dto.QuotaResponse;
import com.notification.quota.exception.QuotaNotFoundException;
import com.notification.quota.model.Channel;
import com.notification.quota.model.Quota;
import com.notification.quota.repository.QuotaCacheRepository;
import com.notification.quota.repository.QuotaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaService {

    private final QuotaRepository quotaRepository;
    private final QuotaCacheRepository quotaCacheRepository;

    public static final String QUOTA_NOT_FOUND = "Quota not found for clientId=%s, channel=%s";

    @Transactional(readOnly = true)
    public QuotaResponse getQuota(String clientId, Channel channel) {
        log.info("Getting quota for clientId={}, channel={}", clientId, channel);

        Quota quota = quotaRepository.findByClientIdAndChannel(clientId, channel)
                .orElseThrow(() -> new QuotaNotFoundException(
                        String.format(QUOTA_NOT_FOUND, clientId, channel)
                ));

        return QuotaResponse.fromEntity(quota);
    }

    @Transactional(readOnly = true)
    public List<QuotaResponse> getAllQuotasByClient(String clientId) {
        log.info("Getting all quotas for clientId={}", clientId);

        List<Quota> quotas = quotaRepository.findByClientId(clientId);

        if (quotas.isEmpty()) {
            log.warn("No quotas found for clientId={}", clientId);
        }

        return quotas.stream()
                .map(QuotaResponse::fromEntity)
                .toList();
    }

    @Transactional
    public QuotaResponse createQuota(QuotaCreateRequest request) {
        log.info("Creating quota for clientId={}, channel={}", request.getClientId(), request.getChannel());

        if (quotaRepository.existsByClientIdAndChannel(request.getClientId(), request.getChannel())) {
            throw new IllegalArgumentException(
                    String.format("Quota already exists for clientId=%s, channel=%s",
                            request.getClientId(), request.getChannel())
            );
        }

        Quota quota = Quota.builder()
                .clientId(request.getClientId())
                .channel(request.getChannel())
                .totalQuota(request.getTotalQuota())
                .usedQuota(0L)
                .availableQuota(request.getTotalQuota())
                .active(request.getActive())
                .build();

        Quota savedQuota = quotaRepository.save(quota);

        quotaCacheRepository.saveAvailableQuota(
                savedQuota.getClientId(),
                savedQuota.getChannel(),
                savedQuota.getAvailableQuota()
        );

        log.info("Quota created successfully with id={}", savedQuota.getId());
        return QuotaResponse.fromEntity(savedQuota);
    }

    @Transactional
    public QuotaResponse updateQuotaLimit(String clientId, Channel channel, Long newTotalQuota) {
        log.info("Updating quota limit for clientId={}, channel={}, newTotal={}",
                clientId, channel, newTotalQuota);

        Quota quota = quotaRepository.findByClientIdAndChannel(clientId, channel)
                .orElseThrow(() -> new QuotaNotFoundException(
                        String.format(QUOTA_NOT_FOUND, clientId, channel)
                ));

        Long difference = newTotalQuota - quota.getTotalQuota();
        quota.setTotalQuota(newTotalQuota);
        quota.setAvailableQuota(quota.getAvailableQuota() + difference);

        Quota updatedQuota = quotaRepository.save(quota);

        quotaCacheRepository.saveAvailableQuota(
                updatedQuota.getClientId(),
                updatedQuota.getChannel(),
                updatedQuota.getAvailableQuota()
        );

        log.info("Quota limit updated successfully");
        return QuotaResponse.fromEntity(updatedQuota);
    }

    @Transactional
    public QuotaResponse resetQuota(String clientId, Channel channel) {
        log.info("Resetting quota for clientId={}, channel={}", clientId, channel);

        Quota quota = quotaRepository.findByClientIdAndChannel(clientId, channel)
                .orElseThrow(() -> new QuotaNotFoundException(
                        String.format(QUOTA_NOT_FOUND, clientId, channel)
                ));

        quota.resetQuota();
        Quota resetQuota = quotaRepository.save(quota);

        quotaCacheRepository.saveAvailableQuota(
                resetQuota.getClientId(),
                resetQuota.getChannel(),
                resetQuota.getAvailableQuota()
        );

        log.info("Quota reset successfully");
        return QuotaResponse.fromEntity(resetQuota);
    }

    @Transactional
    public void deleteQuota(String clientId, Channel channel) {
        log.info("Deleting quota for clientId={}, channel={}", clientId, channel);

        Quota quota = quotaRepository.findByClientIdAndChannel(clientId, channel)
                .orElseThrow(() -> new QuotaNotFoundException(
                        String.format(QUOTA_NOT_FOUND, clientId, channel)
                ));

        quotaRepository.delete(quota);
        quotaCacheRepository.invalidate(clientId, channel);

        log.info("Quota deleted successfully");
    }

    @Transactional(readOnly = true)
    public List<QuotaResponse> getQuotasNearLimit(Long threshold) {
        log.info("Getting quotas near limit with threshold={}", threshold);

        List<Quota> quotas = quotaRepository.findQuotasNearLimit(threshold);

        return quotas.stream()
                .map(QuotaResponse::fromEntity)
                .toList();
    }

}