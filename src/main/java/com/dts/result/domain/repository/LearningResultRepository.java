package com.dts.result.domain.repository;

import com.dts.result.domain.entity.LearningResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningResultRepository extends JpaRepository<LearningResultEntity, UUID> {

    Optional<LearningResultEntity> findBySourceIdAndSourceType(UUID sourceId, String sourceType);

    @Query("SELECT COALESCE(MAX(r.attemptNo), 0) FROM LearningResultEntity r WHERE r.userId = :userId AND r.targetType = :targetType AND r.targetId = :targetId")
    int findMaxAttemptNo(
            @Param("userId") UUID userId,
            @Param("targetType") String targetType,
            @Param("targetId") UUID targetId
    );

    Page<LearningResultEntity> findByUserId(UUID userId, Pageable pageable);
}
