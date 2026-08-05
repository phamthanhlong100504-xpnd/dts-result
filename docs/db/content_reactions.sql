-- Table: content_reactions
-- Service: lms-tracking / dts-result
-- Engine: yugabyte / postgresql
-- Entities mapped: content-reaction
-- Mô tả: Phản ứng nhanh của learner lên content item (lesson / courseware / course); unique per (target_type, target_id, user_id, reaction_type). Tách riêng khỏi discussion_reactions vì target là content item trong service khác (không có FK cứng — cross-service ref qua UUID). Cho phép toggle like/unlike theo từng loại phản ứng. Denormalize count KHÔNG dùng ở tầng DB — aggregate trực tiếp tại query thời điểm gọi (count nhỏ, index đủ nhanh).

CREATE TABLE content_reactions (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(), -- Khóa chính UUID
    tenant_id     UUID        NOT NULL, -- Tenant isolation
    target_type   TEXT        NOT NULL, -- Loại content bị react: lesson / courseware / course
    target_id     UUID        NOT NULL, -- ID của content item (cross-service ref, không FK cứng)
    user_id       UUID        NOT NULL, -- ID learner thực hiện reaction (IAM contract)
    reaction_type TEXT        NOT NULL DEFAULT 'like', -- Loại phản ứng: like / heart / helpful / insightful / disagree
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW() -- Thời điểm react
);

ALTER TABLE content_reactions
    ADD CONSTRAINT pk_content_reactions PRIMARY KEY (id),
    ADD CONSTRAINT uq_content_reactions_target_user_type
        UNIQUE (tenant_id, target_type, target_id, user_id, reaction_type),
    ADD CONSTRAINT ck_content_reactions_target_type
        CHECK (target_type IN ('lesson', 'courseware', 'course')),
    ADD CONSTRAINT ck_content_reactions_reaction_type
        CHECK (reaction_type IN ('like', 'heart', 'helpful', 'insightful', 'disagree'));

COMMENT ON COLUMN content_reactions.id            IS 'Khóa chính UUID';
COMMENT ON COLUMN content_reactions.tenant_id     IS 'Tenant isolation';
COMMENT ON COLUMN content_reactions.target_type   IS 'Loại content: lesson / courseware / course';
COMMENT ON COLUMN content_reactions.target_id     IS 'ID của content item (cross-service ref, không FK cứng)';
COMMENT ON COLUMN content_reactions.user_id       IS 'ID learner thực hiện reaction (IAM contract)';
COMMENT ON COLUMN content_reactions.reaction_type IS 'Loại phản ứng: like / heart / helpful / insightful / disagree';
COMMENT ON COLUMN content_reactions.created_at    IS 'Thời điểm react';

-- Tra cứu counts/check theo target (query chính: GetContentReactionCounts + IsContentReacted)
CREATE INDEX ix_content_reactions_target ON content_reactions (tenant_id, target_type, target_id);
-- Tra cứu danh sách content learner đã react (query phụ)
CREATE INDEX ix_content_reactions_user   ON content_reactions (tenant_id, user_id);
