-- Table: user_content_results
-- Service: lms-result
-- Engine: yugabyte
-- Entities mapped: node-learning-progress
--
-- Read model tiến độ học tập dạng CÂY theo từng learner (1 row = 1 node).
-- 1 row / (user, content_code): PK = uuidv5(user_id || content_id || content_code), KHÔNG gồm version.
-- content_version_id là thuộc tính (version learner đang học tại node), bất biến sau khi tạo row.
-- Giữ status/percent + rollup chỉ số học từ media_result / document_result / user_content_attempts.
-- Cây: adjacency (parent_node_id) + materialized path (content_code = chuỗi UUID nội dung).
--
-- answers contract (array of objects, nullable) — dùng cho leaf node học 1 lần (video tương tác,
-- audio quiz, câu hỏi nhúng trong bài giảng...); không dùng cho container node hoặc bài thi/quiz
-- nhiều lần nộp (dùng user_content_attempts.answers thay thế):
-- [
--   {
--     "question_id":         "uuid",     -- questions.id
--     "question_version_id": "uuid",     -- question_versions.id — snapshot version lúc trả lời
--     "answer":              { ... },    -- nội dung trả lời, polymorphic theo question_type:
--                                        --   SINGLE_CHOICE:    {"option_id": "uuid"}
--                                        --   MULTIPLE_CHOICE:  {"option_ids": ["uuid", ...]}
--                                        --   TRUE_FALSE:       {"value": true}
--                                        --   TRUE_FALSE_MULTI: {"statements": [{"id":"uuid","value":true}, ...]}
--                                        --   FILL_BLANK:       {"blanks": [{"id":"uuid","value":"text"}, ...]}
--                                        --   DRAG_TO_BLANK:    {"blanks": [{"id":"uuid","token_id":"uuid"}, ...]}
--                                        --   DRAG_TO_IMAGE:    {"zones": [{"id":"uuid","token_id":"uuid"}, ...]}
--                                        --   SHORT_ANSWER:     {"text": "..."}
--                                        --   ESSAY/READING_ESSAY: {"text": "..."}
--                                        --   MATCHING:         {"pairs": [{"left_id":"uuid","right_id":"uuid"}, ...]}
--                                        --   ORDERING:         {"item_ids": ["uuid", ...]}
--                                        --   UNDERLINE:        {"ranges": [{"start":0,"end":5}, ...]}
--                                        --   FIND_FIX_ERROR:   {"corrections": [{"id":"uuid","value":"text"}, ...]}
--     "is_correct":          true,       -- null nếu chờ chấm tay
--     "score":               1.00,       -- null nếu chờ chấm tay
--     "max_score":           2.00        -- null nếu không áp dụng
--   }
-- ]

CREATE TABLE user_content_results (

    -- ── Định danh ──
    id                 uuid        NOT NULL,                          -- PK = uuidv5(user_id || content_id || content_code). Cùng content_id ở 2 vị trí cây → 2 id khác nhau.
    tenant_id          uuid        NOT NULL,                          -- Tenant sở hữu; cột đầu mọi index.
    user_id            uuid        NOT NULL,                          -- Learner sở hữu tiến độ này.

    -- ── Tham chiếu nội dung ──
    content_id         uuid        NOT NULL,                          -- ID đối tượng nội dung node trỏ tới (từ lms-content-builder).
    content_type       text        NOT NULL,                          -- Tên bảng lưu trữ content_id (vd 'videos', 'lessons', 'coursewares').
    content_version_id uuid,                                          -- Version learner đang học tại node. NULLABLE: bảng tổng quát, content không có version → NULL. BẤT BIẾN sau khi tạo row (set lúc INSERT, không đổi khi UPDATE).

    -- ── Cấu trúc cây ──
    parent_node_id     uuid,                                          -- id node cha trực tiếp; NULL = gốc. Dùng bubble-up.
    content_code       text        NOT NULL,                          -- Materialized path: chuỗi UUID nội dung từ gốc tới node, vd 'UUID1:UUID2:UUID3'. Prefix-scan: content_code LIKE x || ':%'.

    -- ── Tiến độ ──
    status             text        NOT NULL DEFAULT 'NOT_COMPLETED',  -- NOT_COMPLETED (mặc định) / COMPLETED.
    percent            int4        NOT NULL DEFAULT 0,                -- Phần trăm hoàn thành (0–100). Container = số lá COMPLETED / tổng lá dưới nhánh * 100.

    -- ── Rollup phiên học ──
    total_learn_sec    int4        NOT NULL DEFAULT 0,                -- Tổng thời gian học (giây) = SUM(duration_sec) từ media_result + document_result. Container = SUM lá con.
    learn_count        int4        NOT NULL DEFAULT 0,                -- Số lần học (mở xem/đọc) = COUNT phiên bị động (media_result + document_result). Container = SUM lá con.

    -- ── Rollup nộp bài / trả lời ──
    attempt_count      int4        NOT NULL DEFAULT 0,                -- Số lần nộp/trả lời = COUNT(user_content_attempts). Container = SUM lá con.
    best_score         numeric(10,2),                                 -- Điểm cao nhất qua các lần nộp. NULL = chưa nộp hoặc không chấm điểm.
    last_score         numeric(10,2),                                 -- Điểm lần nộp gần nhất. NULL = chưa nộp hoặc không chấm điểm.
    last_score_at      timestamptz,                                   -- Thời điểm chấm/nộp gần nhất. NULL = chưa nộp.
    last_attempt_id    uuid,                                          -- ID lần nộp gần nhất trong user_content_attempts. Join nhanh không cần ORDER BY.

    -- ── Câu trả lời (leaf 1 lần) ──
    answers            jsonb,                                         -- Mảng câu trả lời per-question cho leaf học 1 lần (video/audio tương tác...). NULL = không có câu hỏi hoặc là container node. Xem contract đầu file.

    -- ── Dữ liệu đặc thù ──
    metadata           jsonb,                                         -- Dữ liệu đặc thù từng loại node, ít query trực tiếp. NULL = không có.

    -- ── Mốc thời gian ──
    started_at         timestamptz,                                   -- Thời điểm bắt đầu học lần đầu. NULL = chưa bắt đầu.
    last_activity_at   timestamptz,                                   -- Thời điểm hoạt động gần nhất. Dùng sort "học gần đây" và resume.
    completed_at       timestamptz,                                   -- Thời điểm COMPLETED. NULL = chưa hoàn thành.

    -- ── Audit ──
    created_at         timestamptz NOT NULL DEFAULT NOW(),            -- Thời điểm bản ghi tạo lần đầu.
    updated_at         timestamptz                                    -- Thời điểm worker tính lại gần nhất. NULL = chưa cập nhật sau khi tạo.
);

-- ── Constraints ──
ALTER TABLE user_content_results
    ADD CONSTRAINT pk_user_content_results  PRIMARY KEY (id),
    ADD CONSTRAINT ck_ucr_status            CHECK (status IN ('NOT_COMPLETED','COMPLETED')),
    ADD CONSTRAINT ck_ucr_percent           CHECK (percent BETWEEN 0 AND 100),
    ADD CONSTRAINT ck_ucr_total_learn       CHECK (total_learn_sec >= 0),
    ADD CONSTRAINT ck_ucr_learn_count       CHECK (learn_count >= 0),
    ADD CONSTRAINT ck_ucr_attempt_count     CHECK (attempt_count >= 0),
    ADD CONSTRAINT ck_ucr_best_score        CHECK (best_score IS NULL OR best_score >= 0),
    ADD CONSTRAINT ck_ucr_last_score        CHECK (last_score  IS NULL OR last_score  >= 0),
    ADD CONSTRAINT ck_ucr_answers_array     CHECK (answers IS NULL OR jsonb_typeof(answers) = 'array');

-- ── Comments ──
COMMENT ON TABLE user_content_results IS
    'Read model tiến độ học tập dạng cây theo learner (1 row = 1 node). '
    'Giữ status/percent + rollup từ media_result / document_result / user_content_attempts. '
    'Cây dùng adjacency (parent_node_id) + materialized path (content_code).';

COMMENT ON COLUMN user_content_results.id               IS 'PK = uuidv5(user_id+content_id+content_code). Cùng content_id ở 2 vị trí cây → 2 id khác nhau.';
COMMENT ON COLUMN user_content_results.tenant_id        IS 'Tenant sở hữu; cột đầu mọi index.';
COMMENT ON COLUMN user_content_results.user_id          IS 'Learner sở hữu tiến độ này.';
COMMENT ON COLUMN user_content_results.content_id       IS 'ID đối tượng nội dung (từ lms-content-builder).';
COMMENT ON COLUMN user_content_results.content_type     IS 'Tên bảng lưu trữ content_id (vd videos, lessons, coursewares).';
COMMENT ON COLUMN user_content_results.content_version_id IS 'Version nội dung learner đang học tại node. NULL hợp lệ: content không có version. Bất biến sau khi tạo row (set lúc INSERT, giữ nguyên khi UPDATE).';
COMMENT ON COLUMN user_content_results.parent_node_id   IS 'id cha trực tiếp; NULL = gốc. Worker bubble-up qua cột này.';
COMMENT ON COLUMN user_content_results.content_code     IS 'Materialized path = chuỗi UUID nội dung từ gốc (vd UUID1:UUID2:UUID3). Prefix-scan: content_code LIKE x || '':%''.';
COMMENT ON COLUMN user_content_results.status           IS 'NOT_COMPLETED (mặc định) / COMPLETED.';
COMMENT ON COLUMN user_content_results.percent          IS '0–100. Container = số lá COMPLETED / tổng lá dưới nhánh * 100.';
COMMENT ON COLUMN user_content_results.total_learn_sec  IS 'Tổng thời gian học (giây) = SUM(duration_sec) từ media_result + document_result. Container = SUM lá con.';
COMMENT ON COLUMN user_content_results.learn_count      IS 'Số lần học (mở xem/đọc) = COUNT phiên bị động (media_result + document_result). Container = SUM lá con.';
COMMENT ON COLUMN user_content_results.attempt_count    IS 'Số lần nộp/trả lời = COUNT(user_content_attempts). Container = SUM lá con.';
COMMENT ON COLUMN user_content_results.best_score       IS 'Điểm cao nhất qua các lần nộp. NULL = chưa nộp hoặc không chấm điểm.';
COMMENT ON COLUMN user_content_results.last_score       IS 'Điểm lần nộp gần nhất. NULL = chưa nộp hoặc không chấm điểm.';
COMMENT ON COLUMN user_content_results.last_score_at    IS 'Thời điểm chấm/nộp gần nhất. NULL = chưa nộp.';
COMMENT ON COLUMN user_content_results.last_attempt_id  IS 'ID lần nộp gần nhất trong user_content_attempts. Join nhanh không cần ORDER BY.';
COMMENT ON COLUMN user_content_results.answers          IS
    'Mảng câu trả lời per-question cho leaf học 1 lần (video/audio tương tác, câu hỏi nhúng bài giảng...). '
    'NULL nếu node không có câu hỏi hoặc là container node. '
    'Mỗi phần tử: {question_id, question_version_id, answer (polymorphic theo question_type), '
    'is_correct (null nếu chờ chấm tay), score (null nếu chờ chấm tay), max_score}. '
    'Xem contract đầy đủ ở comment đầu file.';
COMMENT ON COLUMN user_content_results.metadata         IS 'Dữ liệu đặc thù từng loại node, ít query trực tiếp. NULL = không có.';
COMMENT ON COLUMN user_content_results.started_at       IS 'Thời điểm bắt đầu học lần đầu. NULL = chưa bắt đầu.';
COMMENT ON COLUMN user_content_results.last_activity_at IS 'Thời điểm hoạt động gần nhất. Dùng sort "học gần đây" và resume.';
COMMENT ON COLUMN user_content_results.completed_at     IS 'Thời điểm COMPLETED. NULL = chưa hoàn thành.';
COMMENT ON COLUMN user_content_results.created_at       IS 'Thời điểm bản ghi tạo lần đầu.';
COMMENT ON COLUMN user_content_results.updated_at       IS 'Thời điểm worker tính lại gần nhất. NULL = chưa cập nhật sau khi tạo.';

-- ── Indexes ──
-- Ràng buộc "1 user 1 content_code" trực tiếp ở DB (khớp công thức uuidv5 của PK).
CREATE UNIQUE INDEX uq_ucr_user_content
    ON user_content_results (tenant_id, user_id, content_id, content_code);

-- Tìm con trực tiếp + bubble-up theo cha
CREATE INDEX ix_ucr_parent
    ON user_content_results (tenant_id, user_id, parent_node_id);

-- Quét/sửa/dời toàn nhánh con theo prefix content_code
CREATE INDEX ix_ucr_content_code
    ON user_content_results (tenant_id, user_id, content_code text_pattern_ops);

-- Worker tìm mọi row bị ảnh hưởng khi 1 đối tượng nội dung thay đổi
CREATE INDEX ix_ucr_content
    ON user_content_results (tenant_id, content_type, content_id);

-- Dashboard learner: lọc theo trạng thái + sắp xếp theo hoạt động gần nhất
CREATE INDEX ix_ucr_user_status_activity
    ON user_content_results (tenant_id, user_id, status, last_activity_at);

-- Query metadata đặc thù: resume video, trang tài liệu
CREATE INDEX gin_ucr_metadata
    ON user_content_results USING GIN (metadata jsonb_path_ops);

-- Query answers per-question (xem lại bài làm, kiểm tra câu trả lời leaf)
CREATE INDEX gin_ucr_answers
    ON user_content_results USING GIN (answers jsonb_path_ops)
    WHERE answers IS NOT NULL;
