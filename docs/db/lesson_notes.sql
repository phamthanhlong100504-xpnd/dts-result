-- Table: lesson_notes
-- Service: lms-tracking / dts-result
-- Engine: yugabyte / postgresql
-- Mô tả: Ghi chú học tập của learner, gắn vào một bài giảng (lesson). Mỗi learner có tối đa 1 ghi chú per bài giảng (unique trên tenant_id + user_id + lesson_id). Ghi chú cấp bài giảng lưu trong `content` (plain text); ghi chú per-block lưu trong JSONB `block_notes` dưới dạng mảng `[{block_id, content}]`. Private — chỉ learner tự xem. Soft-delete qua `deleted_at`.

CREATE TABLE lesson_notes (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL,                       -- Tenant scope; mọi truy vấn phải filter theo cột này.
    user_id     UUID        NOT NULL,                       -- Learner sở hữu ghi chú.
    lesson_id   UUID        NOT NULL,                       -- ID bài giảng.
    content     TEXT,                                       -- Ghi chú cấp bài giảng (plain text); NULL nếu chỉ có block notes.
    block_notes JSONB       NOT NULL DEFAULT '[]'::jsonb,   -- Mảng ghi chú per-block: [{block_id, content}].
    metadata    JSONB       NOT NULL DEFAULT '{}'::jsonb,   -- Metadata phụ trợ (app_version, device, locale, session_id...).
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,                                -- Soft delete; NULL = chưa xóa.
    created_by  UUID,                                       -- Audit: caller tạo ghi chú.
    updated_by  UUID                                        -- Audit: caller sửa ghi chú lần cuối.
);

ALTER TABLE lesson_notes
    ADD CONSTRAINT pk_lesson_notes PRIMARY KEY (id),
    ADD CONSTRAINT uq_lesson_notes_user_lesson UNIQUE (tenant_id, user_id, lesson_id),
    ADD CONSTRAINT ck_lesson_notes_block_notes_array CHECK (jsonb_typeof(block_notes) = 'array'),
    ADD CONSTRAINT ck_lesson_notes_metadata_object CHECK (jsonb_typeof(metadata) = 'object'),
    ADD CONSTRAINT ck_lesson_notes_updated_gte_created CHECK (updated_at >= created_at);

COMMENT ON TABLE lesson_notes IS 'Ghi chú học tập của learner gắn vào bài giảng. 1 learner / 1 bài giảng → 1 row duy nhất. Block-level notes lưu trong JSONB block_notes.';
COMMENT ON COLUMN lesson_notes.id IS 'Khóa chính.';
COMMENT ON COLUMN lesson_notes.tenant_id IS 'Tenant scope; mọi truy vấn phải filter theo cột này.';
COMMENT ON COLUMN lesson_notes.user_id IS 'Learner sở hữu ghi chú.';
COMMENT ON COLUMN lesson_notes.lesson_id IS 'ID bài giảng.';
COMMENT ON COLUMN lesson_notes.content IS 'Ghi chú cấp bài giảng (plain text); NULL nếu chỉ có block notes.';
COMMENT ON COLUMN lesson_notes.block_notes IS 'Mảng ghi chú per-block: [{block_id: uuid, content: text}].';
COMMENT ON COLUMN lesson_notes.metadata IS 'Metadata phụ trợ khi tạo/sửa ghi chú (app_version, device, locale, session_id...).';
COMMENT ON COLUMN lesson_notes.created_at IS 'Thời điểm tạo ghi chú.';
COMMENT ON COLUMN lesson_notes.updated_at IS 'Thời điểm cập nhật ghi chú lần cuối (lesson-level hoặc bất kỳ block note nào thay đổi).';
COMMENT ON COLUMN lesson_notes.deleted_at IS 'Soft delete; NULL = chưa xóa.';
COMMENT ON COLUMN lesson_notes.created_by IS 'Audit: caller tạo ghi chú.';
COMMENT ON COLUMN lesson_notes.updated_by IS 'Audit: caller sửa ghi chú lần cuối.';

-- Query danh sách ghi chú của learner, mới nhất trước
CREATE INDEX ix_lesson_notes_user_updated ON lesson_notes (tenant_id, user_id, updated_at DESC) WHERE deleted_at IS NULL;
-- Query ghi chú theo bài giảng cụ thể
CREATE INDEX ix_lesson_notes_lesson ON lesson_notes (tenant_id, lesson_id) WHERE deleted_at IS NULL;
