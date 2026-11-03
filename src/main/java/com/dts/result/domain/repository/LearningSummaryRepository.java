package com.dts.result.domain.repository;

import com.dts.result.domain.entity.LearningSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningSummaryRepository extends JpaRepository<LearningSummaryEntity, UUID> {

    Optional<LearningSummaryEntity> findByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    List<LearningSummaryEntity> findByUserId(UUID userId);
}
