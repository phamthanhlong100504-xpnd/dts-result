package com.dts.result.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_content_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContentAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "node_id")
    private UUID nodeId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "device_kind")
    private String deviceKind;

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(name = "heartbeat_at")
    private OffsetDateTime heartbeatAt;

    @Column(name = "ip_address", columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    private String ipAddress;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "ended_reason")
    private String endedReason;

    @Column(name = "content_version_id")
    private UUID contentVersionId;

    @Column(name = "source_service")
    private String sourceService;

    @Column(name = "source_ref")
    private String sourceRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "auto_submitted_at")
    private OffsetDateTime autoSubmittedAt;

    @Column(name = "graded_at")
    private OffsetDateTime gradedAt;

    @Column(name = "grading_status", nullable = false)
    private String gradingStatus;

    @Column(name = "score", precision = 10, scale = 2)
    private BigDecimal score;

    @Column(name = "max_score", precision = 10, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "penalty_score", precision = 10, scale = 2, nullable = false)
    private BigDecimal penaltyScore;

    @Column(name = "final_score", precision = 10, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "is_late", nullable = false)
    private Boolean isLate;

    @Column(name = "grader_id")
    private UUID graderId;

    @Column(name = "time_taken_sec", nullable = false)
    private Integer timeTakenSec;

    @Column(name = "hint_used_count", nullable = false)
    private Integer hintUsedCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", columnDefinition = "jsonb")
    private String answers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proctoring_data", columnDefinition = "jsonb")
    private String proctoringData;
}
