package com.notification.quota.repository;

import com.notification.quota.model.Channel;
import com.notification.quota.model.Quota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaRepository extends JpaRepository<Quota, Long> {

    Optional<Quota> findByClientIdAndChannel(String clientId, Channel channel);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM Quota q WHERE q.clientId = :clientId AND q.channel = :channel")
    Optional<Quota> findByClientIdAndChannelWithLock(
            @Param("clientId") String clientId,
            @Param("channel") Channel channel
    );

    List<Quota> findByClientId(String clientId);

    List<Quota> findByClientIdAndActive(String clientId, Boolean active);

    @Query("SELECT q FROM Quota q WHERE q.availableQuota < :threshold AND q.active = true")
    List<Quota> findQuotasNearLimit(@Param("threshold") Long threshold);

    boolean existsByClientIdAndChannel(String clientId, Channel channel);

}