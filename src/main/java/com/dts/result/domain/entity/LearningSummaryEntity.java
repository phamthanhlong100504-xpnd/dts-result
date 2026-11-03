package com.dts.result.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "last_result_id")
    private UUID lastResultId;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "completion_count", nullable = false)
    private Integer completionCount;

    @Column(name = "best_score", precision = 10, scale = 2)
    private BigDecimal bestScore;

    @Column(name = "latest_score", precision = 10, scale = 2)
    private BigDecimal latestScore;

    @Column(name = "average_score", precision = 10, scale = 2)
    private BigDecimal averageScore;

    @Column(name = "progress", precision = 5, scale = 2, nullable = false)
    private BigDecimal progress;

    @Column(name = "total_duration_seconds", nullable = false)
    private Integer totalDurationSeconds;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "last_activity_at")
    private OffsetDateTime lastActivityAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary_snapshot", columnDefinition = "jsonb")
    private String summarySnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
