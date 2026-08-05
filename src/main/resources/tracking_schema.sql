-- =============================================================================
-- SERVICE: dts-tracking / dts-result (User Interaction & Event Tracking)
-- Database Engine: PostgreSQL / YugabyteDB
-- File: tracking_schema.sql
-- Mô tả: File DDL gộp các bảng theo dõi sự kiện thời gian thực và tương tác học viên
--        (tracking events, ghi chú bài học, thả tim/reaction, bookmark nội dung).
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- 1. Table: tracking_events (Sự kiện vòng đời học tập - Event Store Hot ~30 ngày)
-- =============================================================================
CREATE TABLE tracking_events (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id        UUID        NOT NULL,
    user_id          UUID        NOT NULL,
    event_type       TEXT        NOT NULL,
    entity_kind      TEXT        NOT NULL,
    entity_id        UUID        NOT NULL,
    version_id       UUID        NOT NULL,
    version_no       INT4        NOT NULL,
    language         TEXT        NOT NULL,
    block_id         UUID,
    node_path        TEXT,
    occurred_at      TIMESTAMPTZ NOT NULL,
    received_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source           TEXT        NOT NULL,
    idempotency_key  TEXT        NOT NULL,
    payload          JSONB       NOT NULL DEFAULT '{}'::jsonb,
    context          JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by       UUID,

    CONSTRAINT pk_tracking_events PRIMARY KEY (id),
    CONSTRAINT uq_tracking_events_tenant_idempotency UNIQUE (tenant_id, idempotency_key),
    CONSTRAINT ck_tracking_events_event_type CHECK (event_type IN ('lesson.started','lesson.completed','block.entered','block.left','block.completed')),
    CONSTRAINT ck_tracking_events_entity_kind CHECK (entity_kind IN ('lesson','courseware')),
    CONSTRAINT ck_tracking_events_source CHECK (source IN ('client_web','client_mobile','service:delivery','service:assignment','service:discussion','service:access')),
    CONSTRAINT ck_tracking_events_version_no_positive CHECK (version_no >= 1),
    CONSTRAINT ck_tracking_events_received_after_occurred CHECK (received_at >= occurred_at),
    CONSTRAINT ck_tracking_events_block_required CHECK ((event_type LIKE 'block.%') = (block_id IS NOT NULL AND node_path IS NOT NULL)),
    CONSTRAINT ck_tracking_events_payload_object CHECK (jsonb_typeof(payload) = 'object'),
    CONSTRAINT ck_tracking_events_context_object CHECK (jsonb_typeof(context) = 'object')
);

CREATE INDEX ix_tracking_events_user_occurred ON tracking_events (tenant_id, user_id, occurred_at);
CREATE INDEX ix_tracking_events_entity_version_occurred ON tracking_events (tenant_id, entity_kind, entity_id, version_id, occurred_at);
CREATE INDEX ix_tracking_events_entity_occurred ON tracking_events (tenant_id, entity_kind, entity_id, occurred_at);
CREATE INDEX ix_tracking_events_block_occurred ON tracking_events (tenant_id, version_id, block_id, occurred_at) WHERE block_id IS NOT NULL;
CREATE INDEX ix_tracking_events_type_occurred ON tracking_events (tenant_id, event_type, occurred_at);
CREATE INDEX ix_tracking_events_received ON tracking_events (received_at);


-- =============================================================================
-- 2. Table: lesson_notes (Ghi chú bài học cá nhân của learner)
-- =============================================================================
CREATE TABLE lesson_notes (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    lesson_id   UUID        NOT NULL,
    content     TEXT,
    block_notes JSONB       NOT NULL DEFAULT '[]'::jsonb,
    metadata    JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,
    created_by  UUID,
    updated_by  UUID,

    CONSTRAINT pk_lesson_notes PRIMARY KEY (id),
    CONSTRAINT uq_lesson_notes_user_lesson UNIQUE (tenant_id, user_id, lesson_id),
    CONSTRAINT ck_lesson_notes_block_notes_array CHECK (jsonb_typeof(block_notes) = 'array'),
    CONSTRAINT ck_lesson_notes_metadata_object CHECK (jsonb_typeof(metadata) = 'object'),
    CONSTRAINT ck_lesson_notes_updated_gte_created CHECK (updated_at >= created_at)
);

CREATE INDEX ix_lesson_notes_user_updated ON lesson_notes (tenant_id, user_id, updated_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_lesson_notes_lesson ON lesson_notes (tenant_id, lesson_id) WHERE deleted_at IS NULL;


-- =============================================================================
-- 3. Table: content_reactions (Phản ứng/Like/Thả tim lên nội dung học)
-- =============================================================================
CREATE TABLE content_reactions (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID        NOT NULL,
    target_type   TEXT        NOT NULL,
    target_id     UUID        NOT NULL,
    user_id       UUID        NOT NULL,
    reaction_type TEXT        NOT NULL DEFAULT 'like',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_content_reactions PRIMARY KEY (id),
    CONSTRAINT uq_content_reactions_target_user_type UNIQUE (tenant_id, target_type, target_id, user_id, reaction_type),
    CONSTRAINT ck_content_reactions_target_type CHECK (target_type IN ('lesson', 'courseware', 'course')),
    CONSTRAINT ck_content_reactions_reaction_type CHECK (reaction_type IN ('like', 'heart', 'helpful', 'insightful', 'disagree'))
);

CREATE INDEX ix_content_reactions_target ON content_reactions (tenant_id, target_type, target_id);
CREATE INDEX ix_content_reactions_user ON content_reactions (tenant_id, user_id);


-- =============================================================================
-- 4. Table: content_bookmarks (Lưu bookmark nội dung bài học/học liệu)
-- =============================================================================
CREATE TABLE content_bookmarks (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    target_type TEXT        NOT NULL,
    target_id   UUID        NOT NULL,
    note        TEXT,
    metadata    JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,
    created_by  UUID,
    updated_by  UUID,

    CONSTRAINT pk_content_bookmarks PRIMARY KEY (id),
    CONSTRAINT uq_content_bookmarks_user_target UNIQUE (tenant_id, user_id, target_type, target_id),
    CONSTRAINT ck_content_bookmarks_target_type CHECK (target_type IN ('CURRICULUM', 'LESSON', 'COURSEWARE')),
    CONSTRAINT ck_content_bookmarks_metadata_object CHECK (jsonb_typeof(metadata) = 'object'),
    CONSTRAINT ck_content_bookmarks_updated_gte_created CHECK (updated_at >= created_at)
);

CREATE INDEX ix_content_bookmarks_user_type_updated ON content_bookmarks (tenant_id, user_id, target_type, updated_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_content_bookmarks_user_updated ON content_bookmarks (tenant_id, user_id, updated_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_content_bookmarks_user_type_target ON content_bookmarks (tenant_id, user_id, target_type, target_id) WHERE deleted_at IS NULL;
