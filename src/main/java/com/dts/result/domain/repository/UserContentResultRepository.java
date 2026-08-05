package com.dts.result.domain.repository;

import com.dts.result.domain.entity.UserContentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserContentResultRepository extends JpaRepository<UserContentResultEntity, UUID> {

    Optional<UserContentResultEntity> findByTenantIdAndId(UUID tenantId, UUID id);

    Optional<UserContentResultEntity> findByTenantIdAndUserIdAndContentId(UUID tenantId, UUID userId, UUID contentId);
}
