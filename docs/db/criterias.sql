-- Table: criterias
-- Service: lms-result
-- Entities mapped: criteria
-- Engine: yugabyte
-- Mô tả: Bộ tiêu chí (root) và tiêu chí con (leaf) tự quan hệ qua parent_id.
--   Root (parent_id IS NULL): định nghĩa bộ — require_all, pass_score, grading.
--   Leaf (parent_id IS NOT NULL): 1 tiêu chí cụ thể — ref_type/ref_ids (tập item),
--     aggregate (cách gộp), operator+threshold (xét pass/fail), weight (trọng số điểm).
--   Thực thể cần đánh giá KHÔNG lưu ở đây — thực thể tự lưu criteria_id phù hợp (loose-ref).
--
-- Cách tính actual_value theo aggregate:
--   AVG      : AVG(raw của từng item)        → normalized = MIN(actual/threshold*100, 100)
--   MAX      : MAX(raw của từng item)        → normalized = MIN(actual/threshold*100, 100)
--   AT_LEAST : COUNT(item pass threshold)    → normalized = MIN(count/min_count*100, 100)
--   ALL      : tất cả item pass threshold?  → normalized = 100 nếu true, 0 nếu false
--   ANY      : bất kỳ item pass threshold?  → normalized = 100 nếu true, 0 nếu false
--
-- raw = user_content_results.[metric_field] — caller chỉ định rõ cột đọc lúc tạo criteria.
-- Tập giá trị hợp lệ của metric_field (cột đo lường trong user_content_results):
--   'percent'         → phần trăm hoàn thành (0–100)
--   'best_score'      → điểm cao nhất qua các lần nộp
--   'last_score'      → điểm lần nộp gần nhất
--   'total_learn_sec' → tổng thời gian học (giây)
--   'learn_count'     → số lần học
--   'attempt_count'   → số lần nộp/trả lời
-- Ví dụ: ref_type='coursewares', courseware_type=VIDEO → metric_field='percent'
--         ref_type='coursewares', courseware_type=ASSESSMENT → metric_field='best_score'
--         ref_type='lessons' → metric_field='percent'

CREATE TABLE criterias (
    id          UUID        NOT NULL,
    tenant_id   UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    deleted_at  TIMESTAMPTZ,

    -- Cây tự quan hệ
    parent_id   UUID,                               -- NULL = root; NOT NULL = leaf → criterias.id

    -- ── ROOT only (parent_id IS NULL) ──
    require_all BOOL,                               -- true = ALL leaf pass → bộ PASSED; false = ANY leaf pass
    pass_score  NUMERIC(8,2),                       -- ngưỡng total_score để bộ PASSED; NULL = chỉ dùng require_all
    grading     JSONB,                              -- [{label,from,to}] xếp loại theo total_score; NULL = không xếp loại

    -- ── LEAF only (parent_id IS NOT NULL) ──
    ref_type     VARCHAR(50),                        -- tên bảng lưu trữ ref_ids (vd 'coursewares', 'lessons'); NULL = toàn content (không lọc theo loại)
    ref_ids      UUID[]      DEFAULT '{}',           -- item cụ thể; rỗng = toàn ref_type
    metric_field VARCHAR(30) NOT NULL DEFAULT 'percent', -- cột đo lường đọc từ user_content_results: percent | best_score | last_score | total_learn_sec | learn_count | attempt_count
    aggregate    VARCHAR(20),                        -- AVG | MAX | AT_LEAST | ALL | ANY
    min_count   SMALLINT,                           -- số item tối thiểu pass; CHỈ dùng khi aggregate=AT_LEAST
    operator    VARCHAR(10),                        -- gte | gt | eq | lte | lt; KHÔNG dùng khi aggregate=AT_LEAST|ALL|ANY
    threshold   NUMERIC(12,2),                      -- ngưỡng pass; KHÔNG dùng khi aggregate=AT_LEAST|ALL|ANY
    weight      NUMERIC(6,2) DEFAULT 1,             -- trọng số đóng góp vào total_score; mặc định 1

    -- ── Chung ──
    title       TEXT,                               -- tên bộ (root) hoặc tên tiêu chí (leaf)

    CONSTRAINT pk_criterias PRIMARY KEY (id),

    CONSTRAINT ck_criterias_aggregate CHECK (
        aggregate IS NULL OR aggregate IN ('AVG','MAX','AT_LEAST','ALL','ANY')
    ),
    CONSTRAINT ck_criterias_operator CHECK (
        operator IS NULL OR operator IN ('gte','gt','eq','lte','lt')
    ),

    -- AT_LEAST: dùng min_count, không dùng operator/threshold
    CONSTRAINT ck_criterias_at_least CHECK (
        aggregate IS DISTINCT FROM 'AT_LEAST'
        OR (min_count IS NOT NULL AND min_count >= 1
            AND operator IS NULL AND threshold IS NULL)
    ),

    -- ALL/ANY: không dùng operator/threshold/min_count (tự tính boolean)
    CONSTRAINT ck_criterias_all_any CHECK (
        aggregate NOT IN ('ALL','ANY')
        OR (operator IS NULL AND threshold IS NULL AND min_count IS NULL)
    ),

    -- AVG/MAX: phải có operator + threshold, không có min_count
    CONSTRAINT ck_criterias_avg_max CHECK (
        aggregate NOT IN ('AVG','MAX')
        OR (operator IS NOT NULL AND threshold IS NOT NULL AND min_count IS NULL)
    ),

    CONSTRAINT ck_criterias_metric_field CHECK (
        metric_field IN ('percent','best_score','last_score','total_learn_sec','learn_count','attempt_count')
    ),
    CONSTRAINT ck_criterias_weight CHECK (weight IS NULL OR weight > 0),
    CONSTRAINT ck_criterias_grading CHECK (grading IS NULL OR jsonb_typeof(grading) = 'array')
);

-- Duyệt leaf theo bộ (root)
CREATE INDEX idx_criterias_parent ON criterias (tenant_id, parent_id) WHERE deleted_at IS NULL;
-- Lookup bộ gốc
CREATE INDEX idx_criterias_root ON criterias (tenant_id, id) WHERE parent_id IS NULL AND deleted_at IS NULL;
