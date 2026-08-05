-- Table: tracking_events
-- Service: lms-tracking / dts-result
-- Engine: yugabyte / postgresql
-- Entities mapped: learning-lifecycle-event
-- Mô tả: Append-only ghi nhận sự kiện vòng đời học tập (start/complete lesson, enter/leave/complete block) từ cả client UI lẫn các backend service (delivery/assignment/discussion/access); entry point duy nhất + source-of-truth cho event vòng đời, fan-out qua outbox + CDC để consumer (lms-result, ClickHouse sink) đồng bộ. Tách 3 trục `(entity_kind, entity_id)` (logical bất biến) + `(version_id, version_no, language)` (bản publish cụ thể) + `(block_id, node_path)` (vị trí node trong snapshot) — cho phép aggregate cross-version và join trực tiếp với `lms-result.user_content_results.node_path`. JSONB `payload`/`context` (denormalize có chủ ý): schema động đa dạng theo `event_type`. Unique `(tenant_id, idempotency_key)` chống ghi trùng khi client retry / service emit lặp. Retention ~30 ngày hot tại YugabyteDB; cold dài hạn ở ClickHouse qua Kafka. Bảng append-only — không có `updated_at`, không có `deleted_at`.

CREATE TABLE tracking_events (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(), -- Khóa chính của event.
    tenant_id        UUID        NOT NULL, -- Tenant phát sinh event; scope mọi truy vấn.
    user_id          UUID        NOT NULL, -- Learner thực hiện hành động (actor của event).
    event_type       TEXT        NOT NULL, -- Loại event vòng đời (`lesson.started` / `lesson.completed` / `block.entered` / `block.left` / `block.completed`).
    entity_kind      TEXT        NOT NULL, -- Loại entity gốc logical (`lesson` / `courseware`); bất biến qua các version.
    entity_id        UUID        NOT NULL, -- ID entity logical (`lessons.id` hoặc `coursewares.id`); KHÔNG đổi qua các lần publish — cho phép aggregate cross-version.
    version_id       UUID        NOT NULL, -- ID bản publish cụ thể user đang học (`lesson_versions.id` / `courseware_*_versions.id` / `practice_set_versions.id`); ref cross-service, không FK cứng.
    version_no       INT4        NOT NULL, -- Số version (denormalize từ version table) — phục vụ filter/report không phải join sang content-builder.
    language         TEXT        NOT NULL, -- Ngôn ngữ của version (denormalize); version key của content-builder là `(entity_id, language, version_no)`.
    block_id         UUID,                 -- ID block trong `blocks_snapshot` của version; chỉ có khi `event_type` là `block.*`; NULL với event `lesson.*`.
    node_path        TEXT,                 -- Path từ root tới block trong snapshot (chuỗi block_id); đồng nhất với `lms-result.user_content_results.node_path`; chỉ có khi `event_type` là `block.*`; NULL với event `lesson.*`.
    occurred_at      TIMESTAMPTZ NOT NULL, -- Thời điểm sự kiện xảy ra thực tế tại nguồn (client/service); khóa retention/partition theo tuần.
    received_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- Thời điểm tracking nhận được event.
    source           TEXT        NOT NULL, -- Nguồn phát event (`client_web` / `client_mobile` / `service:delivery` / `service:assignment` / `service:discussion` / `service:access`).
    idempotency_key  TEXT        NOT NULL, -- Khóa idempotency chống ghi trùng khi client retry hoặc service emit lặp.
    payload          JSONB       NOT NULL DEFAULT '{}'::jsonb, -- Dữ liệu động theo `event_type` (vd: `duration_sec`, `position_sec`, `completion_reason`, `score`, `max_score`, `is_passed`, `leave_reason`).
    context          JSONB       NOT NULL DEFAULT '{}'::jsonb, -- Metadata phụ trợ (`session_id`, `trace_id`, `device`, `app_version`, `locale`, `ip`, `user_agent`).
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- Thời điểm bản ghi được tạo (audit, immutable).
    created_by       UUID                  -- Caller phát event (= `user_id` nếu client tự gửi; NULL nếu service emit thay user).
);

ALTER TABLE tracking_events
    ADD CONSTRAINT pk_tracking_events PRIMARY KEY (id),
    ADD CONSTRAINT uq_tracking_events_tenant_idempotency UNIQUE (tenant_id, idempotency_key),
    ADD CONSTRAINT ck_tracking_events_event_type CHECK (event_type IN ('lesson.started','lesson.completed','block.entered','block.left','block.completed')),
    ADD CONSTRAINT ck_tracking_events_entity_kind CHECK (entity_kind IN ('lesson','courseware')),
    ADD CONSTRAINT ck_tracking_events_source CHECK (source IN ('client_web','client_mobile','service:delivery','service:assignment','service:discussion','service:access')),
    ADD CONSTRAINT ck_tracking_events_version_no_positive CHECK (version_no >= 1),
    ADD CONSTRAINT ck_tracking_events_received_after_occurred CHECK (received_at >= occurred_at),
    ADD CONSTRAINT ck_tracking_events_block_required CHECK ((event_type LIKE 'block.%') = (block_id IS NOT NULL AND node_path IS NOT NULL)),
    ADD CONSTRAINT ck_tracking_events_payload_object CHECK (jsonb_typeof(payload) = 'object'),
    ADD CONSTRAINT ck_tracking_events_context_object CHECK (jsonb_typeof(context) = 'object');

COMMENT ON COLUMN tracking_events.id IS 'Khóa chính của event.';
COMMENT ON COLUMN tracking_events.tenant_id IS 'Tenant phát sinh event; scope mọi truy vấn.';
COMMENT ON COLUMN tracking_events.user_id IS 'Learner thực hiện hành động (actor của event).';
COMMENT ON COLUMN tracking_events.event_type IS 'Loại event vòng đời (`lesson.started` / `lesson.completed` / `block.entered` / `block.left` / `block.completed`).';
COMMENT ON COLUMN tracking_events.entity_kind IS 'Loại entity gốc logical (`lesson` / `courseware`); bất biến qua các version.';
COMMENT ON COLUMN tracking_events.entity_id IS 'ID entity logical (`lessons.id` hoặc `coursewares.id`); KHÔNG đổi qua các lần publish — cho phép aggregate cross-version.';
COMMENT ON COLUMN tracking_events.version_id IS 'ID bản publish cụ thể user đang học (`lesson_versions.id` / `courseware_*_versions.id` / `practice_set_versions.id`); ref cross-service, không FK cứng.';
COMMENT ON COLUMN tracking_events.version_no IS 'Số version (denormalize từ version table) — phục vụ filter/report không phải join sang content-builder.';
COMMENT ON COLUMN tracking_events.language IS 'Ngôn ngữ của version (denormalize); version key của content-builder là `(entity_id, language, version_no)`.';
COMMENT ON COLUMN tracking_events.block_id IS 'ID block trong `blocks_snapshot` của version; chỉ có khi `event_type` là `block.*`; NULL với event `lesson.*`.';
COMMENT ON COLUMN tracking_events.node_path IS 'Path từ root tới block trong snapshot (chuỗi block_id); đồng nhất với `lms-result.user_content_results.node_path`; chỉ có khi `event_type` là `block.*`; NULL với event `lesson.*`.';
COMMENT ON COLUMN tracking_events.occurred_at IS 'Thời điểm sự kiện xảy ra thực tế tại nguồn (client/service); khóa retention/partition theo tuần.';
COMMENT ON COLUMN tracking_events.received_at IS 'Thời điểm tracking nhận được event.';
COMMENT ON COLUMN tracking_events.source IS 'Nguồn phát event (`client_web` / `client_mobile` / `service:delivery` / `service:assignment` / `service:discussion` / `service:access`).';
COMMENT ON COLUMN tracking_events.idempotency_key IS 'Khóa idempotency chống ghi trùng khi client retry hoặc service emit lặp.';
COMMENT ON COLUMN tracking_events.payload IS 'Dữ liệu động theo `event_type` (vd: `duration_sec`, `position_sec`, `completion_reason`, `score`, `max_score`, `is_passed`, `leave_reason`).';
COMMENT ON COLUMN tracking_events.context IS 'Metadata phụ trợ (`session_id`, `trace_id`, `device`, `app_version`, `locale`, `ip`, `user_agent`).';
COMMENT ON COLUMN tracking_events.created_at IS 'Thời điểm bản ghi được tạo (audit, immutable).';
COMMENT ON COLUMN tracking_events.created_by IS 'Caller phát event (= `user_id` nếu client tự gửi; NULL nếu service emit thay user).';

CREATE INDEX ix_tracking_events_user_occurred ON tracking_events (tenant_id, user_id, occurred_at);
CREATE INDEX ix_tracking_events_entity_version_occurred ON tracking_events (tenant_id, entity_kind, entity_id, version_id, occurred_at);
CREATE INDEX ix_tracking_events_entity_occurred ON tracking_events (tenant_id, entity_kind, entity_id, occurred_at);
CREATE INDEX ix_tracking_events_block_occurred ON tracking_events (tenant_id, version_id, block_id, occurred_at) WHERE block_id IS NOT NULL;
CREATE INDEX ix_tracking_events_type_occurred ON tracking_events (tenant_id, event_type, occurred_at);
CREATE INDEX ix_tracking_events_received ON tracking_events (received_at);
