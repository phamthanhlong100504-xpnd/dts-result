package com.dts.result.domain.repository;

import com.dts.result.domain.entity.ContentBookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentBookmarkRepository extends JpaRepository<ContentBookmarkEntity, UUID> {

    Optional<ContentBookmarkEntity> findByTenantIdAndUserIdAndContentId(UUID tenantId, UUID userId, UUID contentId);
}
