package com.dts.result.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "learning_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningSummaryEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "last_result_id")
    private UUID lastResultId;

    @Column(name = "target_type", nullable = false, length = 30)
    private String targetType;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "completion_count", nullable = false)
    @Builder.Default
    private Integer completionCount = 0;

    @Column(name = "best_score", columnDefinition = "numeric")
    private BigDecimal bestScore;

    @Column(name = "latest_score", columnDefinition = "numeric")
    private BigDecimal latestScore;

    @Column(name = "average_score", columnDefinition = "numeric")
    private BigDecimal averageScore;

    @Column(name = "progress", nullable = false, columnDefinition = "numeric")
    @Builder.Default
    private BigDecimal progress = BigDecimal.ZERO;

    @Column(name = "total_duration_seconds", nullable = false)
    @Builder.Default
    private Integer totalDurationSeconds = 0;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "NOT_STARTED";

    @Column(name = "last_activity_at", columnDefinition = "timestamptz")
    private Instant lastActivityAt;

    @Column(name = "completed_at", columnDefinition = "timestamptz")
    private Instant completedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> summarySnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
