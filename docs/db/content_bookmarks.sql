-- Table: content_bookmarks
-- Service: lms-tracking / dts-result
-- Engine: yugabyte / postgresql
-- Mô tả: Bookmark nội dung học tập của user. Mỗi user có đúng 1 bản ghi per (target_type, target_id) — FULL unique trên (tenant_id, user_id, target_type, target_id) (cover cả row đã soft-delete). target_type phân biệt loại nội dung: CURRICULUM | LESSON | COURSEWARE. Bookmark là user preference — không kiểm tra quyền truy cập nội dung khi tạo bookmark. Soft-delete qua `deleted_at`; AddContentBookmark dùng UPSERT (ON CONFLICT) undelete row cũ khi user bookmark lại sau khi đã xóa (giống pattern lesson_notes), không tạo row mới.

CREATE TABLE content_bookmarks (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(), -- Khóa chính UUID.
    tenant_id   UUID        NOT NULL,                        -- Tenant scope; mọi truy vấn phải filter theo cột này.
    user_id     UUID        NOT NULL,                        -- User sở hữu bookmark.
    target_type TEXT        NOT NULL,                        -- Loại nội dung: CURRICULUM | LESSON | COURSEWARE.
    target_id   UUID        NOT NULL,                        -- ID của entity được bookmark (loose reference, không có FK chéo service).
    note        TEXT,                                        -- Ghi chú tuỳ chọn của user; NULL nếu không có.
    metadata    JSONB       NOT NULL DEFAULT '{}'::jsonb,    -- Extension point; {} khi không dùng.
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,                                 -- Soft delete; NULL = chưa xóa.
    created_by  UUID,                                        -- Audit: actor tạo bookmark.
    updated_by  UUID                                         -- Audit: actor sửa lần cuối.
);

ALTER TABLE content_bookmarks
    ADD CONSTRAINT pk_content_bookmarks PRIMARY KEY (id),
    ADD CONSTRAINT uq_content_bookmarks_user_target UNIQUE (tenant_id, user_id, target_type, target_id),
    ADD CONSTRAINT ck_content_bookmarks_target_type CHECK (target_type IN ('CURRICULUM', 'LESSON', 'COURSEWARE')),
    ADD CONSTRAINT ck_content_bookmarks_metadata_object CHECK (jsonb_typeof(metadata) = 'object'),
    ADD CONSTRAINT ck_content_bookmarks_updated_gte_created CHECK (updated_at >= created_at);

COMMENT ON TABLE content_bookmarks IS 'Bookmark nội dung học tập của user. 1 user / 1 entity → tối đa 1 bookmark active (soft-delete aware unique index).';
COMMENT ON COLUMN content_bookmarks.id IS 'Khóa chính; sinh mặc định gen_random_uuid().';
COMMENT ON COLUMN content_bookmarks.tenant_id IS 'Tenant scope; mọi truy vấn phải filter theo cột này.';
COMMENT ON COLUMN content_bookmarks.user_id IS 'User sở hữu bookmark.';
COMMENT ON COLUMN content_bookmarks.target_type IS 'Loại nội dung được bookmark: CURRICULUM | LESSON | COURSEWARE.';
COMMENT ON COLUMN content_bookmarks.target_id IS 'ID của entity được bookmark; loose reference — không có FK chéo service. Caller chịu trách nhiệm truyền ID hợp lệ.';
COMMENT ON COLUMN content_bookmarks.note IS 'Ghi chú tuỳ chọn của user; NULL nếu không có.';
COMMENT ON COLUMN content_bookmarks.metadata IS 'Extension point cho metadata bổ sung; {} khi không dùng.';
COMMENT ON COLUMN content_bookmarks.created_at IS 'Thời điểm tạo bookmark.';
COMMENT ON COLUMN content_bookmarks.updated_at IS 'Thời điểm cập nhật lần cuối (bao gồm cả cập nhật note).';
COMMENT ON COLUMN content_bookmarks.deleted_at IS 'Soft delete; NULL = đang active. UPSERT undelete khi user bookmark lại.';
COMMENT ON COLUMN content_bookmarks.created_by IS 'Audit: actor tạo bookmark (user_id).';
COMMENT ON COLUMN content_bookmarks.updated_by IS 'Audit: actor sửa lần cuối.';

-- Danh sách bookmark của user, filter theo target_type, mới nhất trước
CREATE INDEX ix_content_bookmarks_user_type_updated
    ON content_bookmarks (tenant_id, user_id, target_type, updated_at DESC)
    WHERE deleted_at IS NULL;

-- Danh sách tất cả bookmark của user (cross target_type), mới nhất trước
CREATE INDEX ix_content_bookmarks_user_updated
    ON content_bookmarks (tenant_id, user_id, updated_at DESC)
    WHERE deleted_at IS NULL;

-- Batch check / exact lookup: nhiều target_id cùng lúc cho một user + target_type
CREATE INDEX ix_content_bookmarks_user_type_target
    ON content_bookmarks (tenant_id, user_id, target_type, target_id)
    WHERE deleted_at IS NULL;
