-- Table: completion_criterias
-- Service: lms-result
-- Entities mapped: completion-criteria-set
-- Engine: yugabyte
-- Mô tả: Bộ TỔNG HỢP tiêu chí hoàn thành, gắn 1 content (content_id — cross-service loose-ref, 1 content có thể nhiều bộ). THAM CHIẾU các tiêu chí qua criteria_ids[] (cha giữ id con → tiêu chí tái sử dụng được) và gộp kết quả bằng combine_op (AND=đạt tất cả / OR=đạt bất kỳ). Giữa các bộ cùng content là OR. grading: xếp loại (schema cố định {source, bands:[{label,from,to}]}). Chuyển từ lms-course sang lms-result (service đánh giá hoàn thành).

CREATE TABLE completion_criterias (
    id           UUID         NOT NULL,
    tenant_id    UUID         NOT NULL,
    version      BIGINT       NOT NULL DEFAULT 1,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   UUID,
    updated_by   UUID,
    deleted_at   TIMESTAMPTZ,
    content_id   UUID         NOT NULL,                    -- gắn content (cross-service loose-ref); 1 content có thể nhiều bộ
    title        TEXT,                                     -- tên bộ (tùy chọn)
    combine_op   VARCHAR(10)  NOT NULL DEFAULT 'AND',      -- gộp các tiêu chí tham chiếu: AND (đạt tất cả) | OR (đạt bất kỳ)
    criteria_ids UUID[]       NOT NULL DEFAULT '{}',       -- danh sách id tiêu chí (criterias.id) mà bộ này tổng hợp; giữ thứ tự hiển thị
    grading      JSONB        NOT NULL DEFAULT '{}'::jsonb, -- xếp loại (schema cố định): {source, bands:[{label,from,to}]}; {} = không xếp loại
    metadata     JSONB        NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT pk_completion_criterias PRIMARY KEY (id),
    CONSTRAINT ck_completion_criterias_combine CHECK (combine_op IN ('AND','OR')),
    CONSTRAINT ck_completion_criterias_grading CHECK (jsonb_typeof(grading) = 'object'),
    CONSTRAINT ck_completion_criterias_metadata CHECK (jsonb_typeof(metadata) = 'object')
);
CREATE INDEX idx_completion_criterias_content ON completion_criterias (tenant_id, content_id) WHERE deleted_at IS NULL;
