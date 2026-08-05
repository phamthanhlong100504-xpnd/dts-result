package com.dts.result.application.service;

import com.dts.result.api.form.SaveNoteRequest;
import com.dts.result.api.form.SaveReactionRequest;
import com.dts.result.api.form.ToggleBookmarkRequest;
import com.dts.result.api.response.BookmarkResponse;
import com.dts.result.api.response.LessonNoteResponse;
import com.dts.result.api.response.ReactionResponse;
import com.dts.result.domain.entity.ContentBookmarkEntity;
import com.dts.result.domain.entity.ContentReactionEntity;
import com.dts.result.domain.entity.LessonNoteEntity;
import com.dts.result.domain.repository.ContentBookmarkRepository;
import com.dts.result.domain.repository.ContentReactionRepository;
import com.dts.result.domain.repository.LessonNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInteractionService {

    private final ContentBookmarkRepository bookmarkRepository;
    private final LessonNoteRepository noteRepository;
    private final ContentReactionRepository reactionRepository;

    @Transactional
    public BookmarkResponse toggleBookmark(UUID tenantId, UUID userId, ToggleBookmarkRequest request) {
        log.info("Toggling bookmark for tenant: {}, user: {}, contentId: {}", tenantId, userId, request.contentId());

        Optional<ContentBookmarkEntity> existing = bookmarkRepository.findByTenantIdAndUserIdAndContentId(tenantId, userId, request.contentId());

        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            ContentBookmarkEntity b = existing.get();
            return new BookmarkResponse(b.getId(), tenantId, userId, b.getContentId(), b.getContentType(), b.getNodeId(), b.getNote(), false, b.getCreatedAt());
        }

        ContentBookmarkEntity newBookmark = ContentBookmarkEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .contentId(request.contentId())
                .contentType(request.contentType())
                .nodeId(request.nodeId())
                .note(request.note())
                .build();

        ContentBookmarkEntity saved = bookmarkRepository.save(newBookmark);
        return new BookmarkResponse(saved.getId(), tenantId, userId, saved.getContentId(), saved.getContentType(), saved.getNodeId(), saved.getNote(), true, saved.getCreatedAt());
    }

    @Transactional
    public LessonNoteResponse saveNote(UUID tenantId, UUID userId, SaveNoteRequest request) {
        log.info("Saving note for tenant: {}, user: {}, contentId: {}", tenantId, userId, request.contentId());

        Optional<LessonNoteEntity> existing = noteRepository.findByTenantIdAndUserIdAndContentId(tenantId, userId, request.contentId());

        LessonNoteEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setNoteText(request.noteText());
            entity.setMediaTimestampSec(request.mediaTimestampSec());
            entity.setDocumentPage(request.documentPage());
        } else {
            entity = LessonNoteEntity.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .contentId(request.contentId())
                    .nodeId(request.nodeId())
                    .contentVersionId(request.contentVersionId())
                    .mediaTimestampSec(request.mediaTimestampSec())
                    .documentPage(request.documentPage())
                    .noteText(request.noteText())
                    .build();
        }

        LessonNoteEntity saved = noteRepository.save(entity);
        return new LessonNoteResponse(
                saved.getId(),
                saved.getTenantId(),
                saved.getUserId(),
                saved.getContentId(),
                saved.getNodeId(),
                saved.getContentVersionId(),
                saved.getMediaTimestampSec(),
                saved.getDocumentPage(),
                saved.getNoteText(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Transactional
    public ReactionResponse saveReaction(UUID tenantId, UUID userId, SaveReactionRequest request) {
        log.info("Saving reaction for tenant: {}, user: {}, contentId: {}", tenantId, userId, request.contentId());

        Optional<ContentReactionEntity> existing = reactionRepository.findByTenantIdAndUserIdAndContentId(tenantId, userId, request.contentId());

        ContentReactionEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setReactionType(request.reactionType());
        } else {
            entity = ContentReactionEntity.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .contentId(request.contentId())
                    .contentType(request.contentType())
                    .reactionType(request.reactionType())
                    .build();
        }

        ContentReactionEntity saved = reactionRepository.save(entity);
        return new ReactionResponse(
                saved.getId(),
                saved.getTenantId(),
                saved.getUserId(),
                saved.getContentId(),
                saved.getContentType(),
                saved.getReactionType(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
