package com.vinayms.razorpayclone.common.entity;

import com.vinayms.razorpayclone.common.enums.EventAggregateType;
import com.vinayms.razorpayclone.common.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox",
        indexes = {
                @Index(name = "idx_outbox_status_next_attempt", columnList = "status, next_attempt_at, created_at"),
                @Index(name = "idx_outbox_aggregate", columnList = "aggregate_id, status"),
                @Index(name = "idx_outbox_status_published_at", columnList = "status, published_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class OutboxEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false, length = 30)
    private EventAggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    /** Resolved once at write time; the poller never re-resolves it. */
    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
