package com.notification.quota.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "quotas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"client_id", "channel"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;

    @Column(name = "total_quota", nullable = false)
    private Long totalQuota;

    @Column(name = "used_quota", nullable = false)
    private Long usedQuota;

    @Column(name = "available_quota", nullable = false)
    private Long availableQuota;

    @Column(nullable = false)
    private Boolean active;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void consumeQuota(Long amount) {
        if (this.availableQuota < amount) {
            throw new IllegalStateException("Insufficient quota");
        }
        this.usedQuota += amount;
        this.availableQuota -= amount;
    }

    public void releaseQuota(Long amount) {
        this.usedQuota = Math.max(0, this.usedQuota - amount);
        this.availableQuota = Math.min(this.totalQuota, this.availableQuota + amount);
    }

    public void resetQuota() {
        this.usedQuota = 0L;
        this.availableQuota = this.totalQuota;
    }

}