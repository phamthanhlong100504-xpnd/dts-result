-- =============================================================================
-- Seed Data File: dts-result (Result & Tracking Service)
-- Description: Dữ liệu mẫu (Seed Data) cho các bảng chính thuộc dịch vụ dts-result
-- Format: SQL (PostgreSQL / YugabyteDB) với ON CONFLICT DO NOTHING
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Seed data: learning_results (Kết quả lượt nộp/học tập)
-- -----------------------------------------------------------------------------
INSERT INTO learning_results (
    id,
    user_id,
    source_id,
    target_id,
    source_type,
    target_type,
    attempt_no,
    score,
    max_score,
    progress,
    duration_seconds,
    result,
    started_at,
    completed_at,
    result_snapshot,
    metadata
) VALUES (
    '0190ce1a-4000-7000-8000-000000000001'::uuid,
    '0190ce1a-0000-7000-8000-000000000099'::uuid, -- user_id
    '0190ce1a-7777-7000-8000-000000000001'::uuid, -- source_id
    '0190ce1a-2000-7000-8000-000000000001'::uuid, -- target_id
    'EXAM_SESSION',
    'EXAM',
    1,
    95.00,
    100.00,
    100.00,
    3000,
    'PASSED',
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '1 hour',
    '{"correctQuestions": 19, "wrongQuestions": 1, "passingScore": 80}'::jsonb,
    '{"tabSwitchCount": 0}'::jsonb
) ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 2. Seed data: learning_summaries (Tiến độ tổng hợp)
-- -----------------------------------------------------------------------------
INSERT INTO learning_summaries (
    id,
    user_id,
    target_id,
    last_result_id,
    target_type,
    attempt_count,
    completion_count,
    best_score,
    latest_score,
    average_score,
    progress,
    total_duration_seconds,
    status,
    last_activity_at,
    completed_at,
    summary_snapshot,
    metadata
) VALUES (
    '0190ce1a-0000-7000-8000-000000000001'::uuid,
    '0190ce1a-0000-7000-8000-000000000099'::uuid,
    '0190ce1a-2000-7000-8000-000000000001'::uuid,
    '0190ce1a-4000-7000-8000-000000000001'::uuid,
    'EXAM',
    1,
    1,
    95.00,
    95.00,
    95.00,
    100.00,
    3000,
    'COMPLETED',
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 hour',
    '{"passed": true}'::jsonb,
    '{}'::jsonb
) ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 3. Seed data: tracking_events
-- -----------------------------------------------------------------------------
INSERT INTO tracking_events (
    id,
    tenant_id,
    user_id,
    event_type,
    entity_kind,
    entity_id,
    version_id,
    version_no,
    language,
    block_id,
    node_path,
    occurred_at,
    received_at,
    source,
    idempotency_key,
    payload,
    context
) VALUES (
    '0190ce1a-6000-7000-8000-000000000001'::uuid,
    '0190ce1a-0000-7000-8000-000000000000'::uuid,
    '0190ce1a-0000-7000-8000-000000000099'::uuid,
    'lesson.started',
    'lesson',
    '0190ce1a-2000-7000-8000-000000000002'::uuid,
    '0190ce1a-3000-7000-8000-000000000002'::uuid,
    1,
    'vi-VN',
    NULL,
    NULL,
    NOW() - INTERVAL '12 hours',
    NOW() - INTERVAL '12 hours',
    'client_web',
    'idempotency-key-seed-001',
    '{"device": "desktop", "os": "Windows"}'::jsonb,
    '{"ip": "127.0.0.1"}'::jsonb
) ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 4. Seed data: learning_logs
-- -----------------------------------------------------------------------------
INSERT INTO learning_logs (
    id,
    tenant_id,
    user_id,
    node_id,
    content_id,
    content_type,
    session_kind,
    started_at,
    ended_at,
    duration_sec,
    media_position_sec,
    document_page_read,
    device_kind,
    metadata
) VALUES (
    '0190ce1a-7000-7000-8000-000000000001'::uuid,
    '0190ce1a-0000-7000-8000-000000000000'::uuid,
    '0190ce1a-0000-7000-8000-000000000099'::uuid,
    '0190ce1a-0000-7000-8000-000000000002'::uuid,
    '0190ce1a-2000-7000-8000-000000000002'::uuid,
    'VIDEO',
    'MEDIA_VIEW',
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '30 minutes',
    1800,
    1800,
    NULL,
    'desktop',
    '{"playback_speed": 1.0, "quality": "1080p"}'::jsonb
) ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 5. Seed data: content_bookmarks
-- -----------------------------------------------------------------------------
INSERT INTO content_bookmarks (
    id,
    tenant_id,
    user_id,
    content_id,
    content_type,
    node_id,
    note
) VALUES (
    '0190ce1a-8000-7000-8000-000000000001'::uuid,
    '0190ce1a-0000-7000-8000-000000000000'::uuid,
    '0190ce1a-0000-7000-8000-000000000099'::uuid,
    '0190ce1a-2000-7000-8000-000000000002'::uuid,
    'LESSON',
    '0190ce1a-0000-7000-8000-000000000002'::uuid,
    'Bài học hay cần xem lại trước khi thi'
) ON CONFLICT (tenant_id, user_id, content_id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 6. Seed data: lesson_notes
-- -----------------------------------------------------------------------------
INSERT INTO lesson_notes (
    id,
    tenant_id,
    user_id,
    content_id,
    node_id,
    media_timestamp_sec,
    document_page,
    note_text
) VALUES (
    '0190ce1a-9000-7000-8000-000000000001'::uuid,
    '0190ce1a-0000-7000-8000-000000000000'::uuid,
    '0190ce1a-0000-7000-8000-000000000099'::uuid,
    '0190ce1a-2000-7000-8000-000000000002'::uuid,
    '0190ce1a-0000-7000-8000-000000000002'::uuid,
    450,
    NULL,
    'Cần chú ý định lý và công thức tại phút 7:30'
) ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 7. Seed data: content_reactions
-- -----------------------------------------------------------------------------
INSERT INTO content_reactions (
    id,
    tenant_id,
    user_id,
    content_id,
    content_type,
    reaction_type
) VALUES (
    '0190ce1a-a000-7000-8000-000000000001'::uuid,
    '0190ce1a-0000-7000-8000-000000000000'::uuid,
    '0190ce1a-0000-7000-8000-000000000099'::uuid,
    '0190ce1a-2000-7000-8000-000000000002'::uuid,
    'LESSON',
    'LIKE'
) ON CONFLICT (tenant_id, user_id, content_id) DO NOTHING;
