-- Table: user_content_attempts
-- Service: lms-result
-- Engine: yugabyte
-- Entities mapped: attempt-record
-- Mô tả: Lần NỘP có chấm của 1 learner cho 1 bài tập/thi/quiz. Mỗi row = 1 attempt; số lần nộp = COUNT/`MAX(seq_no)` theo (`tenant_id`,`user_id`,`content_id`). Row MUTABLE trong lúc đang làm (`draft`/`in_progress`, lưu nháp) rồi chốt khi nộp; bất biến về nội dung sau khi chấm xong (audit).
-- Tách bảng chuyên biệt vì vòng đời pháp lý khác hẳn phiên xem/đọc: tách `status` (vòng đời NỘP) khỏi `grading_status` (vòng đời CHẤM). KHỐI CỘT CHUNG (identity/lifecycle/source) đặt trùng tên+kiểu ở cả 3 bảng result (`media_result`/`document_result`) để worker rollup `user_content_results` đọc/tổng hợp đồng nhất và tránh schema drift.
-- File đính kèm tách sang `submission_attachments`. KHÔNG lưu `certificate_id` (quan hệ ngược — `certificates` FK về submission). `node_id` = `user_content_results.id` (external ref, không FK).
--
-- answers contract (array of objects, nullable):
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
--     "is_correct":          true,       -- null nếu chờ chấm tay (ESSAY, SHORT_ANSWER manual...)
--     "score":               1.00,       -- null nếu chờ chấm tay
--     "max_score":           2.00        -- null nếu không áp dụng
--   }
-- ]

CREATE TABLE user_content_attempts (
    -- ── KHỐI CỘT CHUNG (đồng nhất 3 bảng phiên) ──
    id                  uuid          NOT NULL DEFAULT gen_uuid_v7(), -- Khóa chính; mỗi lần nộp 1 id.
    tenant_id           uuid          NOT NULL, -- Tenant sở hữu bản ghi.
    user_id             uuid          NOT NULL, -- Learner sở hữu lần nộp.
    node_id             uuid, -- Vị trí node trong cây = `user_content_results.id`; external ref, không FK. NULL nếu target ngoài cây.
    content_id          uuid          NOT NULL, -- ID bài tập/đề thi (target); luôn có, denormalize tiện query.
    content_type        text          NOT NULL, -- Loại bài: `ASSIGNMENT`/`EXAM`/`QUIZ`/`QUESTION`.
    seq_no              int4          NOT NULL, -- Số thứ tự lần nộp (1-based) cho cùng `(tenant_id, user_id, content_id)`; attempt number.
    started_at          timestamptz   NOT NULL, -- Thời điểm bắt đầu làm bài.
    ended_at            timestamptz, -- Thời điểm kết thúc phiên làm; NULL khi đang làm.
    status              text          NOT NULL DEFAULT 'draft', -- Vòng đời NỘP: `draft`/`in_progress`/`submitted`/`auto_submitted`/`locked`/`voided`.
    device_kind         text, -- Loại thiết bị (`desktop`/`mobile`/`tablet`); NULL nếu không xác định.
    duration_sec        int4          NOT NULL DEFAULT 0, -- Tổng thời gian phiên làm (giây), gồm cả idle. Thời gian làm thực dùng `time_taken_sec`.
    heartbeat_at        timestamptz, -- Thời điểm nhận heartbeat cuối; liveness khi thi kéo dài; NULL nếu chưa có.
    ip_address          inet, -- IP client lúc làm bài; chứng cứ chống thi hộ & audit trail (bắt buộc về pháp lý); NULL nếu không thu được.
    session_id          uuid, -- Browser/app session ID làm bài; theo dõi phiên thi; NULL nếu không có.
    ended_reason        text, -- Lý do kết thúc: `submitted`/`timeout`/`voided`/`system_error`; NULL khi đang làm.
    content_version_id  uuid, -- Snapshot phiên bản đề thi lúc làm; đề sửa sau khi thi xong vẫn truy đúng; NULL nếu không version.
    source_service      text, -- Service phát sinh (vd `lms-assignment`/`lms-exam`); NULL nếu không rõ.
    source_ref          text, -- ID tham chiếu bên source (vd `submission_id`); NULL nếu không có.
    metadata            jsonb, -- Túi mở rộng (gồm `grading_version` chưa promote); query nhiều thì promote thành cột thật.
    created_at          timestamptz   NOT NULL DEFAULT NOW(), -- Thời điểm tạo bản ghi.
    updated_at          timestamptz   NOT NULL DEFAULT NOW(), -- Thời điểm cập nhật gần nhất (mỗi lưu nháp/chốt/chấm).
    -- ── CỘT CHUYÊN BIỆT SUBMISSION ──
    submitted_at        timestamptz, -- Thời điểm user chủ động nộp; khác `ended_at`; NULL khi chưa nộp.
    auto_submitted_at   timestamptz, -- Thời điểm hết giờ system tự nộp; khác `submitted_at`; NULL nếu không tự nộp.
    graded_at           timestamptz, -- Thời điểm chấm xong; NULL khi chưa chấm.
    grading_status      text          NOT NULL DEFAULT 'pending', -- Vòng đời CHẤM: `pending`/`grading`/`graded`/`returned`.
    score               numeric(10,2), -- Điểm thô đạt được; NULL nếu chưa/không chấm.
    max_score           numeric(10,2), -- Điểm tối đa; NULL nếu không áp dụng.
    penalty_score       numeric(10,2) NOT NULL DEFAULT 0, -- Điểm bị trừ (nộp trễ/dùng hint...); hệ quả, nguyên nhân tách ở `is_late`/`hint_used_count`.
    final_score         numeric(10,2), -- Điểm cuối = `score` - `penalty_score`, persist sẵn tránh bug formula; mọi leaderboard/report đọc field này; NULL khi chưa chấm.
    is_passed           bool, -- Kết quả đạt/không đạt; NULL khi chưa chấm.
    is_late             bool          NOT NULL DEFAULT false, -- Nộp trễ hay không; nguyên nhân của penalty, tách khỏi hệ quả.
    grader_id           uuid, -- Người chấm (instructor/admin); NULL nếu auto-grade hoặc chưa chấm.
    time_taken_sec      int4          NOT NULL DEFAULT 0, -- Thời gian làm bài thực (giây), loại idle; làm quá nhanh = flag gian lận.
    hint_used_count     int4          NOT NULL DEFAULT 0, -- Số lần dùng gợi ý; đầu vào trực tiếp của `penalty_score`.
    answers             jsonb, -- Mảng câu trả lời per-question; NULL nếu không có câu hỏi. Xem contract ở đầu file.
    proctoring_data     jsonb -- Dữ liệu giám sát biến thiên theo hệ proctoring (tab_switch_count/webcam_snapshot_count/integrity_score/clipboard_events); promote key khi cần query.
);

ALTER TABLE user_content_attempts
    ADD CONSTRAINT pk_user_content_attempts PRIMARY KEY (id),
    ADD CONSTRAINT uq_uca_user_content_seq UNIQUE (tenant_id, user_id, content_id, seq_no),
    ADD CONSTRAINT ck_uca_content_type CHECK (content_type IN ('ASSIGNMENT','EXAM','QUIZ','QUESTION')),
    ADD CONSTRAINT ck_uca_status CHECK (status IN ('draft','in_progress','submitted','auto_submitted','locked','voided')),
    ADD CONSTRAINT ck_uca_grading_status CHECK (grading_status IN ('pending','grading','graded','returned')),
    ADD CONSTRAINT ck_uca_ended_reason CHECK (ended_reason IS NULL OR ended_reason IN ('submitted','timeout','voided','system_error')),
    ADD CONSTRAINT ck_uca_device_kind CHECK (device_kind IS NULL OR device_kind IN ('desktop','mobile','tablet')),
    ADD CONSTRAINT ck_uca_seq_no CHECK (seq_no >= 1),
    ADD CONSTRAINT ck_uca_duration CHECK (duration_sec >= 0),
    ADD CONSTRAINT ck_uca_time_taken CHECK (time_taken_sec >= 0),
    ADD CONSTRAINT ck_uca_hint_used CHECK (hint_used_count >= 0),
    ADD CONSTRAINT ck_uca_score CHECK (score IS NULL OR score >= 0),
    ADD CONSTRAINT ck_uca_max_score CHECK (max_score IS NULL OR max_score >= 0),
    ADD CONSTRAINT ck_uca_penalty CHECK (penalty_score >= 0),
    ADD CONSTRAINT ck_uca_final_score CHECK (final_score IS NULL OR final_score >= 0),
    ADD CONSTRAINT ck_uca_answers_array CHECK (answers IS NULL OR jsonb_typeof(answers) = 'array'),
    ADD CONSTRAINT ck_uca_proctoring_object CHECK (proctoring_data IS NULL OR jsonb_typeof(proctoring_data) = 'object');

COMMENT ON COLUMN user_content_attempts.id IS 'Khóa chính; mỗi lần nộp 1 id.';
COMMENT ON COLUMN user_content_attempts.tenant_id IS 'Tenant sở hữu bản ghi.';
COMMENT ON COLUMN user_content_attempts.user_id IS 'Learner sở hữu lần nộp.';
COMMENT ON COLUMN user_content_attempts.node_id IS 'Vị trí node trong cây = `user_content_results.id`; external ref, không FK. NULL nếu target ngoài cây.';
COMMENT ON COLUMN user_content_attempts.content_id IS 'ID bài tập/đề thi (target); luôn có, denormalize tiện query.';
COMMENT ON COLUMN user_content_attempts.content_type IS 'Loại bài: `ASSIGNMENT`/`EXAM`/`QUIZ`/`QUESTION`.';
COMMENT ON COLUMN user_content_attempts.seq_no IS 'Số thứ tự lần nộp (1-based) cho cùng `(tenant_id, user_id, content_id)`; attempt number.';
COMMENT ON COLUMN user_content_attempts.started_at IS 'Thời điểm bắt đầu làm bài.';
COMMENT ON COLUMN user_content_attempts.ended_at IS 'Thời điểm kết thúc phiên làm; NULL khi đang làm.';
COMMENT ON COLUMN user_content_attempts.status IS 'Vòng đời NỘP: `draft`/`in_progress`/`submitted`/`auto_submitted`/`locked`/`voided`.';
COMMENT ON COLUMN user_content_attempts.device_kind IS 'Loại thiết bị (`desktop`/`mobile`/`tablet`); NULL nếu không xác định.';
COMMENT ON COLUMN user_content_attempts.duration_sec IS 'Tổng thời gian phiên làm (giây), gồm cả idle. Thời gian làm thực dùng `time_taken_sec`.';
COMMENT ON COLUMN user_content_attempts.heartbeat_at IS 'Thời điểm nhận heartbeat cuối; liveness khi thi kéo dài; NULL nếu chưa có.';
COMMENT ON COLUMN user_content_attempts.ip_address IS 'IP client lúc làm bài; chứng cứ chống thi hộ & audit trail (bắt buộc về pháp lý); NULL nếu không thu được.';
COMMENT ON COLUMN user_content_attempts.session_id IS 'Browser/app session ID làm bài; theo dõi phiên thi; NULL nếu không có.';
COMMENT ON COLUMN user_content_attempts.ended_reason IS 'Lý do kết thúc: `submitted`/`timeout`/`voided`/`system_error`; NULL khi đang làm.';
COMMENT ON COLUMN user_content_attempts.content_version_id IS 'Snapshot phiên bản đề thi lúc làm; đề sửa sau khi thi xong vẫn truy đúng; NULL nếu không version.';
COMMENT ON COLUMN user_content_attempts.source_service IS 'Service phát sinh (vd `lms-assignment`/`lms-exam`); NULL nếu không rõ.';
COMMENT ON COLUMN user_content_attempts.source_ref IS 'ID tham chiếu bên source (vd `submission_id`); NULL nếu không có.';
COMMENT ON COLUMN user_content_attempts.metadata IS 'Túi mở rộng (gồm `grading_version` chưa promote); query nhiều thì promote thành cột thật.';
COMMENT ON COLUMN user_content_attempts.created_at IS 'Thời điểm tạo bản ghi.';
COMMENT ON COLUMN user_content_attempts.updated_at IS 'Thời điểm cập nhật gần nhất (mỗi lưu nháp/chốt/chấm).';
COMMENT ON COLUMN user_content_attempts.submitted_at IS 'Thời điểm user chủ động nộp; khác `ended_at`; NULL khi chưa nộp.';
COMMENT ON COLUMN user_content_attempts.auto_submitted_at IS 'Thời điểm hết giờ system tự nộp; khác `submitted_at`; NULL nếu không tự nộp.';
COMMENT ON COLUMN user_content_attempts.graded_at IS 'Thời điểm chấm xong; NULL khi chưa chấm.';
COMMENT ON COLUMN user_content_attempts.grading_status IS 'Vòng đời CHẤM: `pending`/`grading`/`graded`/`returned`.';
COMMENT ON COLUMN user_content_attempts.score IS 'Điểm thô đạt được; NULL nếu chưa/không chấm.';
COMMENT ON COLUMN user_content_attempts.max_score IS 'Điểm tối đa; NULL nếu không áp dụng.';
COMMENT ON COLUMN user_content_attempts.penalty_score IS 'Điểm bị trừ (nộp trễ/dùng hint...); hệ quả, nguyên nhân tách ở `is_late`/`hint_used_count`.';
COMMENT ON COLUMN user_content_attempts.final_score IS 'Điểm cuối = `score` - `penalty_score`, persist sẵn tránh bug formula; mọi leaderboard/report đọc field này; NULL khi chưa chấm.';
COMMENT ON COLUMN user_content_attempts.is_passed IS 'Kết quả đạt/không đạt; NULL khi chưa chấm.';
COMMENT ON COLUMN user_content_attempts.is_late IS 'Nộp trễ hay không; nguyên nhân của penalty, tách khỏi hệ quả.';
COMMENT ON COLUMN user_content_attempts.grader_id IS 'Người chấm (instructor/admin); NULL nếu auto-grade hoặc chưa chấm.';
COMMENT ON COLUMN user_content_attempts.time_taken_sec IS 'Thời gian làm bài thực (giây), loại idle; làm quá nhanh = flag gian lận.';
COMMENT ON COLUMN user_content_attempts.hint_used_count IS 'Số lần dùng gợi ý; đầu vào trực tiếp của `penalty_score`.';
COMMENT ON COLUMN user_content_attempts.answers IS
    'Mảng câu trả lời per-question; NULL nếu không có câu hỏi. '
    'Mỗi phần tử: {question_id, question_version_id, answer (polymorphic theo question_type), '
    'is_correct (null nếu chờ chấm tay), score (null nếu chờ chấm tay), max_score}. '
    'Xem contract đầy đủ ở comment đầu file.';
COMMENT ON COLUMN user_content_attempts.proctoring_data IS 'Dữ liệu giám sát biến thiên theo hệ proctoring (tab_switch_count/webcam_snapshot_count/integrity_score/clipboard_events); promote key khi cần query.';


-- Đếm số lần / duyệt theo node trong cây
CREATE INDEX ix_uca_user_node_started ON user_content_attempts (tenant_id, user_id, node_id, started_at);
-- Đếm số lần nộp + lịch sử theo bài của 1 learner
CREATE INDEX ix_uca_user_content ON user_content_attempts (tenant_id, user_id, content_id, started_at);
-- Thống kê/leaderboard theo bài (mọi learner)
CREATE INDEX ix_uca_content ON user_content_attempts (tenant_id, content_type, content_id, started_at);
-- Hàng đợi chấm: tìm bài chờ chấm theo trạng thái chấm
CREATE INDEX ix_uca_grading_status ON user_content_attempts (tenant_id, grading_status, submitted_at);
-- Truy ngược về bản ghi nguồn bên source service
CREATE INDEX ix_uca_source_ref ON user_content_attempts (source_service, source_ref);
-- Query answers per-question (phổ điểm, item analysis)
CREATE INDEX gin_uca_answers ON user_content_attempts USING GIN (answers jsonb_path_ops);
