package com.dts.result.domain.repository;

import com.dts.result.domain.entity.TrackingEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEventEntity, UUID> {

    Optional<TrackingEventEntity> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey);

    Page<TrackingEventEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId, Pageable pageable);
}
