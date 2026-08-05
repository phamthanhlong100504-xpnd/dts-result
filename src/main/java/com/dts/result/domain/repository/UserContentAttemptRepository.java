package com.dts.result.domain.repository;

import com.dts.result.domain.entity.UserContentAttemptEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserContentAttemptRepository extends JpaRepository<UserContentAttemptEntity, UUID> {

    @Query("SELECT COALESCE(MAX(a.seqNo), 0) FROM UserContentAttemptEntity a WHERE a.tenantId = :tenantId AND a.userId = :userId AND a.contentId = :contentId")
    int findMaxSeqNoByTenantIdAndUserIdAndContentId(
            @Param("tenantId") UUID tenantId,
            @Param("userId") UUID userId,
            @Param("contentId") UUID contentId
    );

    Optional<UserContentAttemptEntity> findByTenantIdAndId(UUID tenantId, UUID id);

    Page<UserContentAttemptEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId, Pageable pageable);
}
