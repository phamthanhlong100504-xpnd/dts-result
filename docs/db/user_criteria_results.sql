-- Table: user_criteria_results
-- Service: lms-result
-- Entities mapped: user-criteria-result
-- Engine: yugabyte
-- Mô tả: Kết quả đánh giá 1 user theo 1 bộ tiêu chí (criterias root).
--   1 user + 1 bộ = 1 bản ghi hiện hành (is_latest=true).
--   IN_PROGRESS: còn tiêu chí thủ công chưa chấm → UPDATE tại chỗ.
--   Đã chốt (PASSED/FAILED) + đánh giá lại → INSERT bản mới (seq_no+1, is_latest=true),
--     bản cũ is_latest=false (giữ làm lịch sử nếu nghiệp vụ cần).
--
-- Công thức tổng hợp:
--   normalized_score (mỗi leaf) = tính theo aggregate (xem criterias.sql)
--   total_score = SUM(normalized_score * weight) / SUM(weight)  -- 0..100
--   is_passed   = require_all ? ALL(leaf.is_passed) : ANY(leaf.is_passed)
--               AND (pass_score IS NULL OR total_score >= pass_score)
--   grade_label = lookup grading bands theo total_score
--
-- Đánh giá thủ công (MANUAL):
--   Grader nhập score trực tiếp cho leaf → is_passed tự tính: score [operator] threshold
--   status IN_PROGRESS cho đến khi tất cả leaf MANUAL được chấm xong

CREATE TABLE user_criteria_results (
    id          UUID        NOT NULL,
    tenant_id   UUID        NOT NULL,
    criteria_id UUID        NOT NULL,               -- → criterias.id (root)
    user_id     UUID        NOT NULL,

    -- Vòng đời
    seq_no      SMALLINT    NOT NULL DEFAULT 1,     -- 1-based; tăng mỗi lần tạo bản mới sau khi đã chốt
    is_latest   BOOL        NOT NULL DEFAULT true,  -- true = bản hiện hành; false = lịch sử
    status      TEXT        NOT NULL DEFAULT 'IN_PROGRESS',
                                                    -- IN_PROGRESS | PASSED | FAILED

    -- Kết quả tổng hợp (tính lại mỗi khi items thay đổi)
    total_score NUMERIC(8,2),                       -- 0..100; NULL khi chưa đủ dữ liệu (còn leaf chờ thủ công)
    is_passed   BOOL,                               -- NULL khi IN_PROGRESS
    grade_label TEXT,                               -- nhãn từ grading bands của bộ; NULL nếu không xếp loại

    -- Chi tiết từng leaf — snapshot tại thời điểm đánh giá
    items       JSONB       NOT NULL DEFAULT '[]',
    -- Array, mỗi phần tử là 1 leaf:
    -- {
    --   criteria_id:      uuid,        -- criterias.id (leaf)
    --   title:            text,        -- snapshot tên leaf lúc đánh giá
    --   ref_type:         text,        -- snapshot
    --   metric_field:     text,        -- snapshot cột đo lường đọc từ user_content_results
    --   aggregate:        text,        -- snapshot
    --   min_count:        int,         -- snapshot; null nếu không phải AT_LEAST
    --   operator:         text,        -- snapshot; null nếu ALL|ANY|AT_LEAST
    --   threshold:        numeric,     -- snapshot; null nếu ALL|ANY|AT_LEAST
    --   weight:           numeric,     -- snapshot
    --   actual_value:     numeric,     -- giá trị đo được thực tế; null nếu chờ thủ công
    --   normalized_score: numeric,     -- 0..100; null nếu chờ thủ công
    --   is_passed:        bool,        -- null nếu chờ thủ công
    --   eval_type:        text,        -- 'AUTO' | 'MANUAL'
    --   grader_id:        uuid,        -- null nếu AUTO
    --   grader_note:      text,        -- ghi chú của grader
    --   graded_at:        timestamptz  -- thời điểm chấm; null nếu AUTO hoặc chưa chấm
    -- }

    -- Chấm thủ công cấp bộ (grader override nhận xét tổng, không override điểm)
    grader_id   UUID,                               -- null nếu thuần AUTO
    grader_note TEXT,
    graded_at   TIMESTAMPTZ,

    -- Mốc thời gian
    evaluated_at TIMESTAMPTZ,                       -- lần tính/chấm gần nhất
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_user_criteria_results PRIMARY KEY (id),
    CONSTRAINT uq_ucr_seq UNIQUE (tenant_id, criteria_id, user_id, seq_no),
    CONSTRAINT ck_ucr_status CHECK (status IN ('IN_PROGRESS','PASSED','FAILED')),
    CONSTRAINT ck_ucr_seq_no CHECK (seq_no >= 1),
    CONSTRAINT ck_ucr_total_score CHECK (total_score IS NULL OR total_score BETWEEN 0 AND 100),
    CONSTRAINT ck_ucr_items CHECK (jsonb_typeof(items) = 'array')
);

-- Bản hiện hành: 1 user 1 bộ chỉ có 1 is_latest=true
CREATE UNIQUE INDEX uq_ucr_latest
    ON user_criteria_results (tenant_id, criteria_id, user_id)
    WHERE is_latest = true;

-- Dashboard user: toàn bộ kết quả hiện hành của 1 user
CREATE INDEX idx_ucr_user
    ON user_criteria_results (tenant_id, user_id, is_latest);

-- Thống kê theo bộ tiêu chí: bao nhiêu user passed
CREATE INDEX idx_ucr_criteria
    ON user_criteria_results (tenant_id, criteria_id, status)
    WHERE is_latest = true;
