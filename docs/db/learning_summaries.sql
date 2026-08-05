-- Table: learning_summaries
-- Service: dts-result
-- Entities mapped: LearningSummary
-- Engine: PostgreSQL
-- Mô tả: Lưu trữ thông tin tổng hợp về quá trình và kết quả học tập của người dùng đối với một đối tượng học tập (như bài thi, bài tập, bài học).
--
-- Cung cấp cái nhìn tổng quan về tiến độ, điểm số, và trạng thái của người dùng. Mỗi người dùng có một bản ghi tổng hợp duy nhất cho mỗi đối tượng học tập.

CREATE TABLE learning_summaries (
    id UUID NOT NULL DEFAULT gen_random_uuid(),    -- Khóa chính
    user_id UUID NOT NULL,    -- Người dùng (tham chiếu tới dts-identity)
    target_id UUID NOT NULL,    -- ID đối tượng học (bài thi, bài tập, chương, bài học, chương trình học)
    last_result_id UUID,    -- FK tới learning_results gần nhất
    target_type VARCHAR(30) NOT NULL,    -- Loại đối tượng học (EXAM, PRACTICE_SET, CHAPTER, LESSON, LEARNING_PROGRAM)
    attempt_count INT NOT NULL DEFAULT 0,    -- Tổng số lần thực hiện
    completion_count INT NOT NULL DEFAULT 0,    -- Số lần hoàn thành
    best_score NUMERIC,    -- Điểm cao nhất đạt được
    latest_score NUMERIC,    -- Điểm của lần thực hiện gần nhất
    average_score NUMERIC,    -- Điểm trung bình của tất cả các lần
    progress NUMERIC NOT NULL DEFAULT 0,    -- Tiến độ hiện tại (%)
    total_duration_seconds INT NOT NULL DEFAULT 0,    -- Tổng thời gian học tích lũy (giây)
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',    -- Trạng thái học tập (NOT_STARTED, IN_PROGRESS, COMPLETED)
    last_activity_at TIMESTAMPTZ,    -- Lần hoạt động gần nhất của người dùng
    completed_at TIMESTAMPTZ,    -- Thời điểm hoàn thành đối tượng học tập
    summary_snapshot JSONB,    -- Snapshot tổng hợp hiện tại ( {"completedChapters": 12, "totalChapters": 15, "completedLessons": 48, "totalLessons": 60, "passed": true})
    metadata JSONB,    -- Dữ liệu mở rộng trong tương lai
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- Thời điểm tạo bản ghi
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP    -- Thời điểm cập nhật cuối cùng
);

ALTER TABLE learning_summaries
    ADD CONSTRAINT pk_learning_summaries PRIMARY KEY (id),
    ADD CONSTRAINT ck_learning_summaries_target_type CHECK (target_type IN ('EXAM', 'PRACTICE_SET', 'CHAPTER', 'LESSON', 'LEARNING_PROGRAM')),
    ADD CONSTRAINT ck_learning_summaries_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'));

COMMENT ON COLUMN learning_summaries.id IS 'Khóa chính';
COMMENT ON COLUMN learning_summaries.user_id IS 'Người dùng (tham chiếu tới dts-identity)';
COMMENT ON COLUMN learning_summaries.target_id IS 'ID đối tượng học (bài thi, bài tập, chương, bài học, chương trình học)';
COMMENT ON COLUMN learning_summaries.last_result_id IS 'FK tới learning_results gần nhất';
COMMENT ON COLUMN learning_summaries.target_type IS 'Loại đối tượng học (EXAM, PRACTICE_SET, CHAPTER, LESSON, LEARNING_PROGRAM)';
COMMENT ON COLUMN learning_summaries.attempt_count IS 'Tổng số lần thực hiện';
COMMENT ON COLUMN learning_summaries.completion_count IS 'Số lần hoàn thành';
COMMENT ON COLUMN learning_summaries.best_score IS 'Điểm cao nhất đạt được';
COMMENT ON COLUMN learning_summaries.latest_score IS 'Điểm của lần thực hiện gần nhất';
COMMENT ON COLUMN learning_summaries.average_score IS 'Điểm trung bình của tất cả các lần';
COMMENT ON COLUMN learning_summaries.progress IS 'Tiến độ hiện tại (%)';
COMMENT ON COLUMN learning_summaries.total_duration_seconds IS 'Tổng thời gian học tích lũy (giây)';
COMMENT ON COLUMN learning_summaries.status IS 'Trạng thái học tập (NOT_STARTED, IN_PROGRESS, COMPLETED)';
COMMENT ON COLUMN learning_summaries.last_activity_at IS 'Lần hoạt động gần nhất của người dùng';
COMMENT ON COLUMN learning_summaries.completed_at IS 'Thời điểm hoàn thành đối tượng học tập';
COMMENT ON COLUMN learning_summaries.summary_snapshot IS 'Snapshot tổng hợp hiện tại (VD: {"completedChapters": 12, "totalChapters": 15, "completedLessons": 48, "totalLessons": 60, "passed": true})';
COMMENT ON COLUMN learning_summaries.metadata IS 'Dữ liệu mở rộng trong tương lai';
COMMENT ON COLUMN learning_summaries.created_at IS 'Thời điểm tạo bản ghi';
COMMENT ON COLUMN learning_summaries.updated_at IS 'Thời điểm cập nhật cuối cùng';

-- Đảm bảo mỗi người dùng chỉ có một bản ghi tổng hợp cho một đối tượng học tập
CREATE UNIQUE INDEX uq_learning_summaries_user_target ON learning_summaries (user_id, target_type, target_id);

-- Hỗ trợ truy vấn nhanh lịch sử học tập của một người dùng
CREATE INDEX ix_learning_summaries_user_id ON learning_summaries (user_id);

-- Hỗ trợ truy vấn trạng thái học tập (vd: lọc các khóa học đang học)
CREATE INDEX ix_learning_summaries_status ON learning_summaries (status);

-- Trigger: auto-update updated_at on row modification
CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_learning_summaries_updated_at
    BEFORE UPDATE ON learning_summaries
    FOR EACH ROW
    EXECUTE FUNCTION trigger_set_updated_at();
