-- =============================================================================
-- Master Schema File: dts-result (Result & Tracking Service)
-- Description: Tạo đầy đủ 11 bảng cơ sở dữ liệu cho dts-result theo đúng thứ tự phụ thuộc
-- Engine: PostgreSQL / YugabyteDB
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Table: criterias
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS criterias (
    id          UUID        NOT NULL,
    tenant_id   UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    deleted_at  TIMESTAMPTZ,
    parent_id   UUID,
    require_all BOOL,
    pass_score  NUMERIC(8,2),
    grading     JSONB,
    ref_type     VARCHAR(50),
    ref_ids      UUID[]      DEFAULT '{}',
    metric_field VARCHAR(30) NOT NULL DEFAULT 'percent',
    aggregate    VARCHAR(20),
    min_count   SMALLINT,
    operator    VARCHAR(10),
    threshold   NUMERIC(12,2),
    weight      NUMERIC(6,2) DEFAULT 1,
    title       TEXT,
    CONSTRAINT pk_criterias PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 2. Table: completion_criterias
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS completion_criterias (
    id           UUID         NOT NULL,
    tenant_id    UUID         NOT NULL,
    version      BIGINT       NOT NULL DEFAULT 1,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   UUID,
    updated_by   UUID,
    deleted_at   TIMESTAMPTZ,
    content_id   UUID         NOT NULL,
    title        TEXT,
    combine_op   VARCHAR(10)  NOT NULL DEFAULT 'AND',
    criteria_ids UUID[]       NOT NULL DEFAULT '{}',
    grading      JSONB        NOT NULL DEFAULT '{}'::jsonb,
    metadata     JSONB        NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT pk_completion_criterias PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 3. Table: user_criteria_results
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_criteria_results (
    id          UUID        NOT NULL,
    tenant_id   UUID        NOT NULL,
    criteria_id UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    seq_no      SMALLINT    NOT NULL DEFAULT 1,
    is_latest   BOOL        NOT NULL DEFAULT true,
    status      TEXT        NOT NULL DEFAULT 'IN_PROGRESS',
    total_score NUMERIC(8,2),
    is_passed   BOOL,
    grade_label TEXT,
    items       JSONB       NOT NULL DEFAULT '[]',
    grader_id   UUID,
    grader_note TEXT,
    graded_at   TIMESTAMPTZ,
    evaluated_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_criteria_results PRIMARY KEY (id),
    CONSTRAINT uq_ucr_seq UNIQUE (tenant_id, criteria_id, user_id, seq_no)
);

-- -----------------------------------------------------------------------------
-- 4. Table: user_content_results
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_content_results (
    id                 UUID        NOT NULL,
    tenant_id          UUID        NOT NULL,
    user_id            UUID        NOT NULL,
    content_id         UUID        NOT NULL,
    content_type       TEXT        NOT NULL,
    content_version_id UUID,
    parent_node_id     UUID,
    content_code       TEXT        NOT NULL,
    status             TEXT        NOT NULL DEFAULT 'NOT_COMPLETED',
    percent            INT4        NOT NULL DEFAULT 0,
    total_learn_sec    INT4        NOT NULL DEFAULT 0,
    learn_count        INT4        NOT NULL DEFAULT 0,
    attempt_count      INT4        NOT NULL DEFAULT 0,
    best_score         NUMERIC(10,2),
    last_score         NUMERIC(10,2),
    last_score_at      TIMESTAMPTZ,
    last_attempt_id    UUID,
    answers            JSONB,
    metadata           JSONB,
    started_at         TIMESTAMPTZ,
    last_activity_at   TIMESTAMPTZ,
    completed_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_user_content_results PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 5. Table: user_content_attempts
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_content_attempts (
    id                  UUID          NOT NULL DEFAULT gen_random_uuid(),
    tenant_id           UUID          NOT NULL,
    user_id             UUID          NOT NULL,
    node_id             UUID,
    content_id          UUID          NOT NULL,
    content_type        TEXT          NOT NULL,
    seq_no              INT4          NOT NULL,
    started_at          TIMESTAMPTZ   NOT NULL,
    ended_at            TIMESTAMPTZ,
    status              TEXT          NOT NULL DEFAULT 'draft',
    device_kind         TEXT,
    duration_sec        INT4          NOT NULL DEFAULT 0,
    heartbeat_at        TIMESTAMPTZ,
    ip_address          INET,
    session_id          UUID,
    ended_reason        TEXT,
    content_version_id  UUID,
    source_service      TEXT,
    source_ref          TEXT,
    metadata            JSONB,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    submitted_at        TIMESTAMPTZ,
    auto_submitted_at   TIMESTAMPTZ,
    graded_at           TIMESTAMPTZ,
    grading_status      TEXT          NOT NULL DEFAULT 'pending',
    score               NUMERIC(10,2),
    max_score           NUMERIC(10,2),
    penalty_score       NUMERIC(10,2) NOT NULL DEFAULT 0,
    final_score         NUMERIC(10,2),
    is_passed           BOOL,
    is_late             BOOL          NOT NULL DEFAULT false,
    grader_id           UUID,
    time_taken_sec      INT4          NOT NULL DEFAULT 0,
    hint_used_count     INT4          NOT NULL DEFAULT 0,
    answers             JSONB,
    proctoring_data     JSONB,
    CONSTRAINT pk_user_content_attempts PRIMARY KEY (id),
    CONSTRAINT uq_uca_user_content_seq UNIQUE (tenant_id, user_id, content_id, seq_no)
);

-- -----------------------------------------------------------------------------
-- 6. Table: tracking_events
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
-- 7. Table: learning_logs
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
-- 8. Table: content_bookmarks
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
-- 9. Table: lesson_notes
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
-- 10. Table: content_reactions
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

-- -----------------------------------------------------------------------------
-- 11. Table: content_node_children
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS content_node_children (
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id      UUID        NOT NULL,
    parent_node_id UUID        NOT NULL,
    child_node_id  UUID        NOT NULL,
    seq_order      INT4        NOT NULL DEFAULT 1,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_content_node_children PRIMARY KEY (id)
);
