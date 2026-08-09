package com.dts.result.domain.repository;

import com.dts.result.domain.entity.LearningResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningResultRepository extends JpaRepository<LearningResultEntity, UUID>, JpaSpecificationExecutor<LearningResultEntity> {
    boolean existsBySourceIdAndSourceType(UUID sourceId, String sourceType);
    int countByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType);
    
    List<LearningResultEntity> findTop10ByUserIdOrderByCompletedAtDesc(UUID userId);
    
    List<LearningResultEntity> findByUserIdAndCompletedAtBetweenOrderByCompletedAtAsc(UUID userId, java.time.Instant start, java.time.Instant end);
}
