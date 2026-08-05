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
@Table(name = "user_content_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContentResultEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "content_version_id")
    private UUID contentVersionId;

    @Column(name = "parent_node_id")
    private UUID parentNodeId;

    @Column(name = "content_code", nullable = false)
    private String contentCode;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "percent", nullable = false)
    private Integer percent;

    @Column(name = "total_learn_sec", nullable = false)
    private Integer totalLearnSec;

    @Column(name = "learn_count", nullable = false)
    private Integer learnCount;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "best_score", precision = 10, scale = 2)
    private BigDecimal bestScore;

    @Column(name = "last_score", precision = 10, scale = 2)
    private BigDecimal lastScore;

    @Column(name = "last_score_at")
    private OffsetDateTime lastScoreAt;

    @Column(name = "last_attempt_id")
    private UUID lastAttemptId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", columnDefinition = "jsonb")
    private String answers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "last_activity_at")
    private OffsetDateTime lastActivityAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
