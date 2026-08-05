package com.dts.result.domain.repository;

import com.dts.result.domain.entity.ContentReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentReactionRepository extends JpaRepository<ContentReactionEntity, UUID> {

    Optional<ContentReactionEntity> findByTenantIdAndUserIdAndContentId(UUID tenantId, UUID userId, UUID contentId);
}
