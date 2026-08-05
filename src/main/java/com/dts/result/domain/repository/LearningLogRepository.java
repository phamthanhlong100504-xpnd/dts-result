package com.dts.result.domain.repository;

import com.dts.result.domain.entity.LearningLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearningLogRepository extends JpaRepository<LearningLogEntity, UUID> {

    Page<LearningLogEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId, Pageable pageable);
}
