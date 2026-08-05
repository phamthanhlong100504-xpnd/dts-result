-- Table: learning_results
-- Service: dts-result
-- Entities mapped: LearningResult
-- Engine: PostgreSQL
-- Mô tả: Lưu trữ kết quả chi tiết của từng lần thực hiện (attempt) một phiên học hoặc bài thi.
--
-- Mỗi bản ghi đại diện cho một lần người dùng làm bài, tương ứng với một session cụ thể. Bảng này lưu trữ điểm số, trạng thái kết quả, và thời gian thực hiện của lần đó.

CREATE TABLE learning_results (
    id UUID NOT NULL DEFAULT gen_random_uuid(),    -- Khóa chính
    user_id UUID NOT NULL,    -- Người dùng (tham chiếu tới dts-identity)
    source_id UUID NOT NULL,    -- ID session gốc (phiên thi, phiên thực hành...)
    target_id UUID NOT NULL,    -- ID đối tượng học
    source_type VARCHAR(30) NOT NULL,    -- Loại session gốc (EXAM_SESSION, PRACTICE_SESSION, LESSON_SESSION, ASSIGNMENT_SESSION)
    target_type VARCHAR(30) NOT NULL,    -- Loại đối tượng học (EXAM, PRACTICE_SET, CHAPTER, LESSON, LEARNING_PROGRAM)
    attempt_no INT NOT NULL,    -- Số thứ tự của lần thực hiện này
    score NUMERIC,    -- Điểm số đạt được
    max_score NUMERIC,    -- Điểm số tối đa có thể đạt được
    progress NUMERIC NOT NULL DEFAULT 0,    -- Tỷ lệ phần trăm hoàn thành (%)
    duration_seconds INT NOT NULL DEFAULT 0,    -- Thời gian thực hiện (giây)
    result VARCHAR(30) NOT NULL,    -- Trạng thái kết quả của lần thực hiện (PASSED, FAILED, COMPLETED, ABANDONED, EXPIRED)
    started_at TIMESTAMPTZ NOT NULL,    -- Thời điểm bắt đầu thực hiện
    completed_at TIMESTAMPTZ,    -- Thời điểm kết thúc thực hiện
    result_snapshot JSONB,    -- Snapshot chi tiết về kết quả ( {"correctQuestions": 34, "wrongQuestions": 6, "unansweredQuestions": 0, "passingScore": 80, "examVersionId": "..."})
    metadata JSONB,    -- Dữ liệu mở rộng trong tương lai
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- Thời điểm tạo bản ghi
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP    -- Thời điểm cập nhật cuối cùng
);

ALTER TABLE learning_results
    ADD CONSTRAINT pk_learning_results PRIMARY KEY (id),
    ADD CONSTRAINT ck_learning_results_source_type CHECK (source_type IN ('EXAM_SESSION', 'PRACTICE_SESSION', 'LESSON_SESSION', 'ASSIGNMENT_SESSION')),
    ADD CONSTRAINT ck_learning_results_target_type CHECK (target_type IN ('EXAM', 'PRACTICE_SET', 'CHAPTER', 'LESSON', 'LEARNING_PROGRAM')),
    ADD CONSTRAINT ck_learning_results_result CHECK (result IN ('PASSED', 'FAILED', 'COMPLETED', 'ABANDONED', 'EXPIRED'));

COMMENT ON COLUMN learning_results.id IS 'Khóa chính';
COMMENT ON COLUMN learning_results.user_id IS 'Người dùng (tham chiếu tới dts-identity)';
COMMENT ON COLUMN learning_results.source_id IS 'ID session gốc (phiên thi, phiên thực hành...)';
COMMENT ON COLUMN learning_results.target_id IS 'ID đối tượng học';
COMMENT ON COLUMN learning_results.source_type IS 'Loại session gốc (EXAM_SESSION, PRACTICE_SESSION, LESSON_SESSION, ASSIGNMENT_SESSION)';
COMMENT ON COLUMN learning_results.target_type IS 'Loại đối tượng học (EXAM, PRACTICE_SET, CHAPTER, LESSON, LEARNING_PROGRAM)';
COMMENT ON COLUMN learning_results.attempt_no IS 'Số thứ tự của lần thực hiện này';
COMMENT ON COLUMN learning_results.score IS 'Điểm số đạt được';
COMMENT ON COLUMN learning_results.max_score IS 'Điểm số tối đa có thể đạt được';
COMMENT ON COLUMN learning_results.progress IS 'Tỷ lệ phần trăm hoàn thành (%)';
COMMENT ON COLUMN learning_results.duration_seconds IS 'Thời gian thực hiện (giây)';
COMMENT ON COLUMN learning_results.result IS 'Trạng thái kết quả của lần thực hiện (PASSED, FAILED, COMPLETED, ABANDONED, EXPIRED)';
COMMENT ON COLUMN learning_results.started_at IS 'Thời điểm bắt đầu thực hiện';
COMMENT ON COLUMN learning_results.completed_at IS 'Thời điểm kết thúc thực hiện';
COMMENT ON COLUMN learning_results.result_snapshot IS 'Snapshot chi tiết về kết quả (VD: {"correctQuestions": 34, "wrongQuestions": 6, "unansweredQuestions": 0, "passingScore": 80, "examVersionId": "..."})';
COMMENT ON COLUMN learning_results.metadata IS 'Dữ liệu mở rộng trong tương lai';
COMMENT ON COLUMN learning_results.created_at IS 'Thời điểm tạo bản ghi';
COMMENT ON COLUMN learning_results.updated_at IS 'Thời điểm cập nhật cuối cùng';

-- Mỗi session chỉ có một kết quả duy nhất
CREATE UNIQUE INDEX uq_learning_results_source ON learning_results (source_id, source_type);

-- Tránh trùng lặp lần thực hiện (attempt_no) của cùng một người dùng trên một đối tượng
CREATE UNIQUE INDEX uq_learning_results_user_target_attempt ON learning_results (user_id, target_type, target_id, attempt_no);

-- Hỗ trợ truy vấn nhanh các kết quả của một người dùng
CREATE INDEX ix_learning_results_user_id ON learning_results (user_id);

-- Trigger: auto-update updated_at on row modification
CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_learning_results_updated_at
    BEFORE UPDATE ON learning_results
    FOR EACH ROW
    EXECUTE FUNCTION trigger_set_updated_at();
