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
@Table(name = "user_criteria_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCriteriaResultEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "criteria_id", nullable = false)
    private UUID criteriaId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    @Column(name = "is_latest", nullable = false)
    private Boolean isLatest;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "total_score", precision = 8, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "grade_label")
    private String gradeLabel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "items", columnDefinition = "jsonb", nullable = false)
    private String items;

    @Column(name = "grader_id")
    private UUID graderId;

    @Column(name = "grader_note")
    private String graderNote;

    @Column(name = "graded_at")
    private OffsetDateTime gradedAt;

    @Column(name = "evaluated_at")
    private OffsetDateTime evaluatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
