-- Table: content_node_children
-- Service: lms-result
-- Engine: yugabyte
--
-- Read model cấu trúc cây nội dung — denormalized từ lms-content-builder.
-- Mỗi row = 1 node cha THEO VERSION, lưu sẵn danh sách con trực tiếp + weight.
-- Cấu trúc con của một nội dung CHỈ phụ thuộc version (ở node/vị trí nào thì nội dung
-- cũng như nhau nếu cùng version) → bảng key theo (content_id, content_version_id),
-- KHÔNG cần content_code.
-- Dùng cho bubble-up trong UpsertWithBubbleUp:
--   parse content_code của leaf (path content_id) → list ancestor content_ids
--   → với mỗi ancestor: lấy version (đã có result row → giữ version cũ; chưa có → mới nhất)
--   → lookup children+weight theo (content_id, content_version_id) → tính weighted avg.
-- "Version mới nhất" của một content_id = MAX(created_at) → DISTINCT ON (content_id).
-- Sync từ lms-content-builder khi content publish/update (cơ chế sync thiết kế riêng);
-- mỗi version publish = 1 row riêng, không overwrite version cũ.

CREATE TABLE content_node_children (

    -- ── Định danh ──
    tenant_id          UUID        NOT NULL,             -- Tenant sở hữu; cột đầu mọi index.
    content_id         UUID        NOT NULL,             -- ID nội dung từ lms-content-builder. Join với user_content_results.content_id.
    content_version_id UUID        NOT NULL,             -- Version của nội dung. Chiều định danh: mỗi version = 1 row.

    -- ── Phân loại ──
    content_type TEXT        NOT NULL,                   -- Loại node (vd 'lessons', 'coursewares'). Dùng để điền content_type khi INSERT row mới vào user_content_results.

    -- ── Cấu trúc con ──
    children     JSONB       NOT NULL DEFAULT '{}',      -- Map child_content_id → weight (numeric). Ví dụ: {"uuid-A": 2, "uuid-B": 1}. Weight mặc định 1 nếu không có. Chỉ con trực tiếp (depth=1).

    -- ── Audit ──
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),     -- Thời điểm version này được sync lần đầu. "Mới nhất" = MAX(created_at) theo content_id.
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()      -- Thời điểm re-sync gần nhất từ lms-content-builder.
);

-- ── Constraints ──
ALTER TABLE content_node_children
    ADD CONSTRAINT pk_content_node_children PRIMARY KEY (tenant_id, content_id, content_version_id);

-- ── Indexes ──
-- Chọn version mới nhất theo content_id (bubble-up: DISTINCT ON (content_id) ORDER BY created_at DESC)
CREATE INDEX ix_cnc_content_latest
    ON content_node_children (tenant_id, content_id, created_at DESC);

-- ── Comments ──
COMMENT ON TABLE content_node_children IS
    'Read model cấu trúc cây nội dung — 1 row = 1 node cha THEO version. '
    'PK (tenant_id, content_id, content_version_id); children = map{child_content_id: weight}. '
    'Bubble-up UpsertWithBubbleUp: lookup theo (content_id, content_version_id), '
    'version mới nhất = MAX(created_at).';

COMMENT ON COLUMN content_node_children.tenant_id          IS 'Tenant sở hữu; cột đầu mọi index.';
COMMENT ON COLUMN content_node_children.content_id         IS 'ID nội dung từ lms-content-builder. PK cùng tenant_id + content_version_id. Join với user_content_results.content_id.';
COMMENT ON COLUMN content_node_children.content_version_id IS 'Version của nội dung. Chiều định danh: mỗi version = 1 row. NOT NULL.';
COMMENT ON COLUMN content_node_children.content_type       IS 'Loại node (vd lessons, coursewares). Dùng để điền content_type khi INSERT ancestor row mới vào user_content_results.';
COMMENT ON COLUMN content_node_children.children           IS 'Map child_content_id → weight. {"uuid-A": 2, "uuid-B": 1}. Weight mặc định 1. Chỉ con trực tiếp (depth=1).';
COMMENT ON COLUMN content_node_children.created_at         IS 'Thời điểm version này được sync lần đầu. "Mới nhất" = MAX(created_at) theo content_id.';
COMMENT ON COLUMN content_node_children.updated_at         IS 'Thời điểm re-sync gần nhất từ lms-content-builder.';
