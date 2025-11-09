package com.notification.quota.service;

import com.notification.quota.dto.QuotaValidationRequest;
import com.notification.quota.dto.QuotaValidationResponse;
import com.notification.quota.exception.QuotaNotFoundException;
import com.notification.quota.model.Channel;
import com.notification.quota.model.Quota;
import com.notification.quota.model.QuotaUsage;
import com.notification.quota.repository.QuotaCacheRepository;
import com.notification.quota.repository.QuotaRepository;
import com.notification.quota.repository.QuotaUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaValidationService {

    private final QuotaRepository quotaRepository;
    private final QuotaCacheRepository quotaCacheRepository;
    private final QuotaUsageRepository quotaUsageRepository;

    @Transactional
    public QuotaValidationResponse validateAndConsume(QuotaValidationRequest request) {
        log.info("Validating and consuming quota for clientId={}, channel={}, amount={}",
                request.getClientId(), request.getChannel(), request.getAmount());

        String clientId = request.getClientId();
        Channel channel = request.getChannel();
        Long amount = request.getAmount();

        // 1. Tentar do cache primeiro
        Optional<Long> cachedQuota = quotaCacheRepository.getAvailableQuota(clientId, channel);

        if (cachedQuota.isPresent()) {
            Long available = cachedQuota.get();

            if (available >= amount) {
                // Consumir do cache
                quotaCacheRepository.decrementQuota(clientId, channel, amount);

                // Consumir do banco (com lock pessimista)
                consumeQuotaFromDatabase(clientId, channel, amount, request.getNotificationId());

                log.info("Quota consumed successfully from cache");
                return QuotaValidationResponse.allowed(available - amount, amount);
            } else {
                log.warn("Insufficient quota in cache for clientId={}, channel={}", clientId, channel);
                return QuotaValidationResponse.denied(
                        "Insufficient quota",
                        available,
                        amount
                );
            }
        }

        // 2. Cache miss - buscar do banco
        return validateAndConsumeFromDatabase(request);
    }

    private QuotaValidationResponse validateAndConsumeFromDatabase(QuotaValidationRequest request) {
        String clientId = request.getClientId();
        Channel channel = request.getChannel();
        Long amount = request.getAmount();

        // Buscar com lock pessimista
        Quota quota = quotaRepository.findByClientIdAndChannelWithLock(clientId, channel)
                .orElseThrow(() -> new QuotaNotFoundException(
                        String.format(QuotaService.QUOTA_NOT_FOUND, clientId, channel)
                ));

        if (!Boolean.TRUE.equals(quota.getActive())) {
            log.warn("Quota is inactive for clientId={}, channel={}", clientId, channel);
            return QuotaValidationResponse.denied(
                    "Quota is inactive",
                    quota.getAvailableQuota(),
                    amount
            );
        }

        if (quota.getAvailableQuota() < amount) {
            log.warn("Insufficient quota for clientId={}, channel={}, available={}, requested={}",
                    clientId, channel, quota.getAvailableQuota(), amount);
            return QuotaValidationResponse.denied(
                    "Insufficient quota",
                    quota.getAvailableQuota(),
                    amount
            );
        }

        // Consumir quota
        quota.consumeQuota(amount);
        quotaRepository.save(quota);

        // Registrar uso
        recordUsage(clientId, channel, amount, request.getNotificationId(), "CONSUME");

        // Atualizar cache
        quotaCacheRepository.saveAvailableQuota(clientId, channel, quota.getAvailableQuota());

        log.info("Quota consumed successfully from database");
        return QuotaValidationResponse.allowed(quota.getAvailableQuota(), amount);
    }

    @Transactional
    public void releaseQuota(String clientId, Channel channel, Long amount, String notificationId) {
        log.info("Releasing quota for clientId={}, channel={}, amount={}", clientId, channel, amount);

        Quota quota = quotaRepository.findByClientIdAndChannelWithLock(clientId, channel)
                .orElseThrow(() -> new QuotaNotFoundException(
                        String.format(QuotaService.QUOTA_NOT_FOUND, clientId, channel)
                ));

        quota.releaseQuota(amount);
        quotaRepository.save(quota);

        // Registrar uso
        recordUsage(clientId, channel, amount, notificationId, "RELEASE");

        // Atualizar cache
        quotaCacheRepository.incrementQuota(clientId, channel, amount);

        log.info("Quota released successfully");
    }

    @Transactional(readOnly = true)
    public QuotaValidationResponse checkQuota(String clientId, Channel channel, Long amount) {
        log.info("Checking quota for clientId={}, channel={}, amount={}", clientId, channel, amount);

        // Verificar no cache primeiro
        Optional<Long> cachedQuota = quotaCacheRepository.getAvailableQuota(clientId, channel);

        if (cachedQuota.isPresent()) {
            Long available = cachedQuota.get();
            boolean allowed = available >= amount;

            return allowed
                    ? QuotaValidationResponse.allowed(available, amount)
                    : QuotaValidationResponse.denied("Insufficient quota", available, amount);
        }

        // Buscar do banco
        Quota quota = quotaRepository.findByClientIdAndChannel(clientId, channel)
                .orElseThrow(() -> new QuotaNotFoundException(
                        String.format(QuotaService.QUOTA_NOT_FOUND, clientId, channel)
                ));

        // Atualizar cache
        quotaCacheRepository.saveAvailableQuota(clientId, channel, quota.getAvailableQuota());

        boolean allowed = quota.getActive() && quota.getAvailableQuota() >= amount;

        return allowed
                ? QuotaValidationResponse.allowed(quota.getAvailableQuota(), amount)
                : QuotaValidationResponse.denied(getDeniedReason(quota), quota.getAvailableQuota(), amount);
    }

    private static String getDeniedReason(Quota quota) {
        return Boolean.TRUE.equals(quota.getActive()) ? "Insufficient quota" : "Quota is inactive";
    }

    private void consumeQuotaFromDatabase(String clientId, Channel channel, Long amount, String notificationId) {
        Quota quota = quotaRepository.findByClientIdAndChannelWithLock(clientId, channel)
                .orElseThrow(() -> new QuotaNotFoundException(
                        String.format(QuotaService.QUOTA_NOT_FOUND, clientId, channel)
                ));

        quota.consumeQuota(amount);
        quotaRepository.save(quota);

        // Registrar uso
        recordUsage(clientId, channel, amount, notificationId, "CONSUME");
    }

    private void recordUsage(String clientId, Channel channel, Long amount, String notificationId, String operation) {
        QuotaUsage usage = QuotaUsage.builder()
                .clientId(clientId)
                .channel(channel)
                .notificationId(notificationId)
                .amount(amount)
                .operation(operation)
                .build();

        quotaUsageRepository.save(usage);
        log.debug("Quota usage recorded: operation={}, amount={}", operation, amount);
    }

}