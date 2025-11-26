package com.notification.template.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "templates",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"client_id", "channel", "template_code", "version"}
        ),
        indexes = {
                @Index(name = "idx_template_lookup", columnList = "client_id,channel,template_code,active"),
                @Index(name = "idx_client_id", columnList = "client_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id")
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column
    private Channel channel;

    @Column(name = "template_code")
    private String templateCode;

    @Column
    private Integer version;

    @Column
    private String content;

    @Column
    private String subject;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "template_variables", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "variable_name")
    private Set<String> variables;

    @Column
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (version == null) {
            version = 1;
        }
        if (active == null) {
            active = true;
        }
    }
}

