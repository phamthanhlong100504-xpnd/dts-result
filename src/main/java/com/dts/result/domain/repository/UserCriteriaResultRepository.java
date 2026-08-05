package com.dts.result.domain.repository;

import com.dts.result.domain.entity.UserCriteriaResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCriteriaResultRepository extends JpaRepository<UserCriteriaResultEntity, UUID> {

    List<UserCriteriaResultEntity> findByTenantIdAndUserIdAndIsLatestTrue(UUID tenantId, UUID userId);

    Optional<UserCriteriaResultEntity> findByTenantIdAndUserIdAndCriteriaIdAndIsLatestTrue(UUID tenantId, UUID userId, UUID criteriaId);
}
