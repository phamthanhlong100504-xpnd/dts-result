package com.dts.result.domain.repository;

import com.dts.result.domain.entity.LearningSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

@Repository
public interface LearningSummaryRepository extends JpaRepository<LearningSummaryEntity, UUID> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LearningSummaryEntity> findByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    @Query("SELECT s FROM LearningSummaryEntity s WHERE s.userId = :userId " +
           "AND (:targetType IS NULL OR s.targetType = :targetType) " +
           "AND (:status IS NULL OR s.status = :status)")
    Page<LearningSummaryEntity> findSummaries(@Param("userId") UUID userId,
                                              @Param("targetType") String targetType,
                                              @Param("status") String status,
                                              Pageable pageable);

    @Query("SELECT s.status, COUNT(s) FROM LearningSummaryEntity s WHERE s.userId = :userId GROUP BY s.status")
    List<Object[]> countStatusByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(s), " +
           "SUM(CASE WHEN s.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.status = 'NOT_STARTED' THEN 1 ELSE 0 END), " +
           "AVG(s.progress) " +
           "FROM LearningSummaryEntity s " +
           "WHERE s.userId = :userId AND s.targetType = :targetType")
    List<Object[]> aggregateProgress(@Param("userId") UUID userId, @Param("targetType") String targetType);

    @Query("SELECT s FROM LearningSummaryEntity s WHERE s.userId = :userId AND s.status = 'IN_PROGRESS' " +
           "ORDER BY s.lastActivityAt DESC, s.progress DESC LIMIT 1")
    Optional<LearningSummaryEntity> findResumeTarget(@Param("userId") UUID userId);

    @Query("SELECT " +
           "SUM(CASE WHEN s.targetType = 'LEARNING_PROGRAM' AND s.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.targetType = 'LEARNING_PROGRAM' AND s.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.targetType = 'CHAPTER' AND s.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.targetType = 'LESSON' AND s.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.targetType = 'EXAM' THEN s.attemptCount ELSE 0 END), " +
           "SUM(CASE WHEN s.targetType = 'EXAM' THEN s.completionCount ELSE 0 END), " +
           "SUM(CASE WHEN s.targetType = 'EXAM' THEN (s.attemptCount - s.completionCount) ELSE 0 END), " +
           "AVG(s.latestScore), " +
           "MAX(s.bestScore), " +
           "SUM(s.totalDurationSeconds), " +
           "SUM(s.attemptCount), " +
           "MAX(s.lastActivityAt) " +
           "FROM LearningSummaryEntity s WHERE s.userId = :userId")
    List<Object[]> aggregateOverview(@Param("userId") UUID userId);


    List<LearningSummaryEntity> findByUserId(UUID userId);
}
