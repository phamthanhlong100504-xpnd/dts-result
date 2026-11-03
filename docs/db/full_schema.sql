-- =============================================================================
-- Master Schema File: dts-result (Result & Tracking Service)
-- Description: Tạo đầy đủ các bảng cơ sở dữ liệu cho dts-result theo schema mới nhất
-- Engine: PostgreSQL / YugabyteDB
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Table: learning_results (Kết quả chi tiết từng lần nộp/phiên học)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS learning_results (
    id                 UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id            UUID        NOT NULL,
    source_id          UUID        NOT NULL,
    target_id          UUID        NOT NULL,
    source_type        VARCHAR(30) NOT NULL,
    target_type        VARCHAR(30) NOT NULL,
    attempt_no         INT4        NOT NULL,
    score              NUMERIC(10,2),
    max_score          NUMERIC(10,2),
    progress           NUMERIC(5,2) NOT NULL DEFAULT 0,
    duration_seconds   INT4        NOT NULL DEFAULT 0,
    result             VARCHAR(30) NOT NULL,
    started_at         TIMESTAMPTZ NOT NULL,
    completed_at       TIMESTAMPTZ,
    result_snapshot    JSONB,
    metadata           JSONB,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_learning_results PRIMARY KEY (id),
    CONSTRAINT ck_learning_results_source_type CHECK (source_type IN ('EXAM_SESSION', 'PRACTICE_SESSION', 'LESSON_SESSION', 'ASSIGNMENT_SESSION')),
    CONSTRAINT ck_learning_results_target_type CHECK (target_type IN ('EXAM', 'PRACTICE_SET', 'CHAPTER', 'LESSON', 'LEARNING_PROGRAM')),
    CONSTRAINT ck_learning_results_result CHECK (result IN ('PASSED', 'FAILED', 'COMPLETED', 'ABANDONED', 'EXPIRED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_learning_results_source ON learning_results (source_id, source_type);
CREATE UNIQUE INDEX IF NOT EXISTS uq_learning_results_user_target_attempt ON learning_results (user_id, target_type, target_id, attempt_no);
CREATE INDEX IF NOT EXISTS ix_learning_results_user_id ON learning_results (user_id);

-- -----------------------------------------------------------------------------
-- 2. Table: learning_summaries (Tiến độ tổng hợp của người dùng)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS learning_summaries (
    id                     UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id                UUID        NOT NULL,
    target_id              UUID        NOT NULL,
    last_result_id         UUID,
    target_type            VARCHAR(30) NOT NULL,
    attempt_count          INT4        NOT NULL DEFAULT 0,
    completion_count       INT4        NOT NULL DEFAULT 0,
    best_score             NUMERIC(10,2),
    latest_score           NUMERIC(10,2),
    average_score          NUMERIC(10,2),
    progress               NUMERIC(5,2) NOT NULL DEFAULT 0,
    total_duration_seconds INT4        NOT NULL DEFAULT 0,
    status                 VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    last_activity_at       TIMESTAMPTZ,
    completed_at           TIMESTAMPTZ,
    summary_snapshot       JSONB,
    metadata               JSONB,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_learning_summaries PRIMARY KEY (id),
    CONSTRAINT ck_learning_summaries_target_type CHECK (target_type IN ('EXAM', 'PRACTICE_SET', 'CHAPTER', 'LESSON', 'LEARNING_PROGRAM')),
    CONSTRAINT ck_learning_summaries_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_learning_summaries_user_target ON learning_summaries (user_id, target_type, target_id);
CREATE INDEX IF NOT EXISTS ix_learning_summaries_user_id ON learning_summaries (user_id);
CREATE INDEX IF NOT EXISTS ix_learning_summaries_status ON learning_summaries (status);

-- -----------------------------------------------------------------------------
-- 3. Table: tracking_events
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tracking_events (
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
    CONSTRAINT uq_tracking_events_tenant_idempotency UNIQUE (tenant_id, idempotency_key)
);

-- -----------------------------------------------------------------------------
-- 4. Table: learning_logs
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS learning_logs (
    id                 UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id          UUID        NOT NULL,
    user_id            UUID        NOT NULL,
    node_id            UUID,
    content_id         UUID        NOT NULL,
    content_type       TEXT        NOT NULL,
    session_kind       TEXT        NOT NULL,
    started_at         TIMESTAMPTZ NOT NULL,
    ended_at           TIMESTAMPTZ NOT NULL,
    duration_sec       INT4        NOT NULL DEFAULT 0,
    media_position_sec INT4,
    document_page_read INT4,
    device_kind        TEXT,
    metadata           JSONB,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_learning_logs PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 5. Table: content_bookmarks
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS content_bookmarks (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id    UUID        NOT NULL,
    user_id      UUID        NOT NULL,
    content_id   UUID        NOT NULL,
    content_type TEXT        NOT NULL,
    node_id      UUID,
    note         TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_content_bookmarks PRIMARY KEY (id),
    CONSTRAINT uq_content_bookmarks_user_content UNIQUE (tenant_id, user_id, content_id)
);

-- -----------------------------------------------------------------------------
-- 6. Table: lesson_notes
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lesson_notes (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id           UUID        NOT NULL,
    user_id             UUID        NOT NULL,
    content_id          UUID        NOT NULL,
    node_id             UUID,
    content_version_id  UUID,
    media_timestamp_sec INT4,
    document_page       INT4,
    note_text           TEXT        NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_lesson_notes PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 7. Table: content_reactions
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS content_reactions (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID        NOT NULL,
    user_id       UUID        NOT NULL,
    content_id    UUID        NOT NULL,
    content_type  TEXT        NOT NULL,
    reaction_type TEXT        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_content_reactions PRIMARY KEY (id),
    CONSTRAINT uq_content_reactions_user_content UNIQUE (tenant_id, user_id, content_id)
);
