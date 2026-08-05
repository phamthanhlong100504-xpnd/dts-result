package com.dts.result.domain.repository;

import com.dts.result.domain.entity.LessonNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonNoteRepository extends JpaRepository<LessonNoteEntity, UUID> {

    Optional<LessonNoteEntity> findByTenantIdAndUserIdAndContentId(UUID tenantId, UUID userId, UUID contentId);
}
