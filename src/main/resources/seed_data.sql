-- =============================================================================
-- Seed Data File: dts-result (Result & Tracking Service)
-- Description: Dữ liệu mẫu (Seed Data) cho các bảng chính thuộc dịch vụ dts-result
-- Format: SQL (PostgreSQL / YugabyteDB) với ON CONFLICT DO NOTHING
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Seed data: user_content_results (Tiến độ học tập dạng cây)
-- -----------------------------------------------------------------------------
INSERT INTO user_content_results (
    id,
    tenant_id,
    user_id,
    content_id,
    content_type,
    content_version_id,
    parent_node_id,
    content_code,
    status,
    percent,
    total_learn_sec,
    learn_count,
    attempt_count,
    best_score,
    last_score,
    last_score_at,
    last_attempt_id,
    answers,
    metadata,
    started_at,
    last_activity_at,
    completed_at
) VALUES (
    '0190ce1a-0000-7000-8000-000000000001'::uuid,
    '0190ce1a-0000-7000-8000-000000000000'::uuid, -- tenant_id
    '0190ce1a-0000-7000-8000-000000000099'::uuid, -- user_id (learner)
    '0190ce1a-2000-7000-8000-000000000001'::uuid, -- content_id
    'coursewares',
    '0190ce1a-3000-7000-8000-000000000001'::uuid,
    NULL, -- parent_node_id (gốc)
    '0190ce1a-2000-7000-8000-000000000001',
    'COMPLETED',
    100,
    3600,
    5,
    2,
    95.00,
    95.00,
    NOW() - INTERVAL '1 hour',
    '0190ce1a-4000-7000-8000-000000000001'::uuid,
    NULL,
    '{"platform": "web", "browser": "Chrome"}'::jsonb,
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 hour'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO user_content_results (
    id,
    tenant_id,
    user_id,
    content_id,
    content_type,
    content_version_id,
    parent_node_id,
    content_code,
    status,
    percent,
    total_learn_sec,
    learn_count,
    attempt_count,
    best_score,
    last_score,
    last_score_at,
    last_attempt_id,
    answers,
    metadata,
    started_at,
    last_activity_at,
    completed_at
) VALUES (
    '0190ce1a-0000-7000-8000-000000000002'::uuid,
    '0190ce1a-0000-7000-8000-000000000000'::uuid,
    '0190ce1a-0000-7000-8000-000000000099'::uuid,
    '0190ce1a-2000-7000-8000-000000000002'::uuid,
    'lessons',
    '0190ce1a-3000-7000-8000-000000000002'::uuid,
    '0190ce1a-0000-7000-8000-000000000001'::uuid, -- parent_node_id
    '0190ce1a-2000-7000-8000-000000000001:0190ce1a-2000-7000-8000-000000000002',
    'NOT_COMPLETED',
    50,
    1800,
    2,
    1,
    70.00,
    70.00,
    NOW() - INTERVAL '30 minutes',
    '0190ce1a-4000-7000-8000-000000000002'::uuid,
    NULL,
    '{}'::jsonb,
    NOW() - INTERVAL '12 hours',
    NOW() - INTERVAL '30 minutes',
    NULL
) ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 2. Seed data: user_content_attempts (Lịch sử nộp bài tập / bài thi)
-- -----------------------------------------------------------------------------
INSERT INTO user_content_attempts (
    id,
    tenant_id,
    user_id,
    node_id,
    content_id,
    content_type,
    seq_no,
    started_at,
    submitted_at,
    ended_at,
    status,
    grading_status,
    device_kind,
    duration_sec,
    time_taken_sec,
    score,
    max_score,
    penalty_score,
    final_score,
    is_passed,
    is_late,
    hint_used_count,
    answers,
    proctoring_data
) VALUES (
    '0190ce1a-4000-7000-8000-000000000001'::uuid,
    '0190ce1a-0000-7000-8000-000000000000'::uuid,
    '0190ce1a-0000-7000-8000-000000000099'::uuid,
    '0190ce1a-0000-7000-8000-000000000001'::uuid,
    '0190ce1a-2000-7000-8000-000000000001'::uuid,
    'EXAM',
    1,
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 hour',
    'submitted',
    'graded',
    'desktop',
    3600,
    3000,
    95.00,
    100.00,
    0.00,
    95.00,
    TRUE,
    FALSE,
    0,
    '[{"question_id": "0190ce1a-5000-7000-8000-000000000001", "is_correct": true, "score": 95.00}]'::jsonb,
    '{"tab_switch_count": 0, "webcam_enabled": true}'::jsonb
) ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 3. Seed data: tracking_events (Sự kiện vòng đời học tập)
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
-- 4. Seed data: learning_logs (Nhật ký học bị động)
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
-- 5. Seed data: content_bookmarks (Đánh dấu lưu nội dung)
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
-- 6. Seed data: lesson_notes (Ghi chú bài học)
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
-- 7. Seed data: content_reactions (Thả cảm xúc bài học)
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
