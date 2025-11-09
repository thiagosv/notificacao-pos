package com.notification.quota.repository;

import com.notification.quota.model.Channel;
import com.notification.quota.model.QuotaUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuotaUsageRepository extends JpaRepository<QuotaUsage, Long> {

    List<QuotaUsage> findByClientIdOrderByCreatedAtDesc(String clientId);

    List<QuotaUsage> findByClientIdAndChannelOrderByCreatedAtDesc(String clientId, Channel channel);

    @Query("SELECT qu FROM QuotaUsage qu WHERE qu.clientId = :clientId " +
            "AND qu.createdAt BETWEEN :startDate AND :endDate")
    List<QuotaUsage> findByClientIdAndDateRange(
            @Param("clientId") String clientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(qu.amount) FROM QuotaUsage qu WHERE qu.clientId = :clientId " +
            "AND qu.channel = :channel AND qu.operation = 'CONSUME'")
    Long getTotalConsumedByClientIdAndChannel(
            @Param("clientId") String clientId,
            @Param("channel") Channel channel
    );

}