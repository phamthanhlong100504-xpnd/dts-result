# API Blueprint — Tính toán lại tiến độ node

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Tính toán lại tiến độ node API
- **API Type**: Internal / Public
- **Module**: Result Management
- **Feature**: Content Progress Recalculation & Rollup
- **Description**: Thực hiện tính toán lại phần trăm hoàn thành (`percent`), trạng thái (`status`), tổng thời gian học (`total_learn_sec`), số lượt xem/đọc (`learn_count`), và số lần nộp bài (`attempt_count`) cho một node tiến độ (`user_content_results`) dựa trên dữ liệu các lá con hoặc nhật ký học bị động (`media_result`/`document_result`/`user_content_attempts`), sau đó tự động lan truyền (bubble-up) cập nhật lên các node cha theo chuỗi `parent_node_id`.
- **Related Tables**: `user_content_results`, `learning_logs`, `user_content_attempts`
- **Related Services**: `lms-content-builder`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `PUT`
- **URL**: `/api/v1/result-service/content-results/{id}/recalculate`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant sở hữu | Format UUID v4 |
| `X-User-Id` | String | Yes | ID người dùng gửi yêu cầu | Format UUID v4 |

#### Path Variables

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `id` | String | Yes | ID node tiến độ cần tính lại (`user_content_results.id`) | Format UUID v4 |

#### Query Parameters

None.

#### Request Body

None.

---

### Response

- **Success Status**: `200 OK`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `id` | String | ID node tiến độ vừa được tính lại |
| `tenantId` | String | ID tenant |
| `userId` | String | ID học viên |
| `contentCode` | String | Materialized path của node |
| `status` | String | Trạng thái mới sau khi tính toán lại (`NOT_COMPLETED`, `COMPLETED`) |
| `percent` | Integer | Phần trăm mới sau khi tính lại (0–100) |
| `totalLearnSec` | Integer | Tổng thời gian học mới (giây) |
| `learnCount` | Integer | Số lần xem/đọc mới |
| `attemptCount` | Integer | Số lượt nộp bài mới |
| `affectedParentNodesCount` | Integer | Số lượng node cha đã được bubble-up cập nhật thành công |
| `updatedAt` | String | Thời điểm hoàn tất tính toán lại (ISO-8601 UTC) |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Path variable `id` không hợp lệ | Malformed request parameters. Invalid UUID format for node ID. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền tính toán lại node này | Access denied to recalculate content result node. |
| `RES-404-001` | 404 | Không tìm thấy node tiến độ chỉ định | Content result node not found for specified ID and tenant. |
| `RES-500-001` | 500 | Lỗi hệ thống khi tính toán lại tiến độ | An unexpected error occurred while recalculating node progress. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Kiểm tra HTTP Headers `X-Tenant-Id` và `X-User-Id`. Trả về `RES-401-001` nếu thiếu.
2. Kiểm tra Path variable `id` (phải là UUID v4 hợp lệ). Trả về `RES-400-001` nếu sai.
3. Delegate gọi Service layer `RecalculateProgressNodeService`.
4. Trả về thông tin node đã tính toán lại kèm HTTP status `200 OK`.

---

### Service Layer

1. Truy vấn node tiến độ từ Repository theo `(tenantId, id)`. Ném `RES-404-001` nếu không tồn tại.
2. Xác thực quyền hạn: Caller phải là học viên sở hữu node hoặc nhân viên hệ thống/giảng viên/admin.
3. Xử lý tính toán lại các chỉ số cho node target:
   - **Leaf Node**: Tổng hợp `total_learn_sec`, `learn_count` từ `learning_logs` và `attempt_count`, `best_score`, `last_score` từ `user_content_attempts`. Cập nhật `percent = 100` và `status = COMPLETED` nếu thỏa mãn điều kiện hoàn thành.
   - **Container Node**: Tính `percent = (số lá COMPLETED / tổng số lá con) * 100`. Tổng hợp `total_learn_sec = SUM(thời gian các lá con)`. Cập nhật `status = COMPLETED` khi `percent = 100`.
4. Thực thi thuật toán Bubble-up: Duyệt ngược theo `parent_node_id` để tính toán lại từng node cha cấp trên cho tới node gốc (gốc có `parent_node_id IS NULL`).
5. Cập nhật mốc thời gian `updated_at = NOW()`.
6. Trả về kết quả tính toán lại.

---

### Repository Layer

1. Truy vấn bản ghi node target theo `id` và `tenant_id`.
2. Truy vấn danh sách các node con trực tiếp hoặc dưới nhánh theo `parent_node_id` hoặc `content_code LIKE prefix`.
3. Thực thi cập nhật `UPDATE user_content_results` cho node target và các node cha liên quan.

---

### External Interaction

- **Kafka**: Phát sự kiện `lms.result.progress-recalculated` nếu trạng thái node thay đổi từ `NOT_COMPLETED` sang `COMPLETED`.

---

### Validation

#### Request Validation

- `X-Tenant-Id`, `X-User-Id` và `id` phải là UUID v4 hợp lệ.

#### Business Validation

- Node phải tồn tại trong cơ sở dữ liệu.
- Thuật toán bubble-up phải xử lý an toàn chống vòng lặp (cycle detection trên cây).

#### Permission Validation

- Caller phải thuộc phạm vi tenant và có quyền tác động vào tiến độ.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn thông tin node tiến độ target

- **Operation Type**: `SELECT`
- **Target Table**: `user_content_results`
- **Conditions**: `tenant_id = :tenantId AND id = :id`
- **Expected Result**: Trả về thông tin node tiến độ.

---

### Operation 2: Tổng hợp dữ liệu các node con trực tiếp

- **Operation Type**: `SELECT`
- **Target Table**: `user_content_results`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND parent_node_id = :parentNodeId`
- **Expected Result**: Trả về tổng số lá con, số lá con COMPLETED, tổng thời gian học và tổng lượt nộp bài.

---

### Operation 3: Cập nhật chỉ số rollup cho node tiến độ

- **Operation Type**: `UPDATE`
- **Target Table**: `user_content_results`
- **Conditions**: `tenant_id = :tenantId AND id = :id`
- **Expected Result**: Cập nhật `percent = :newPercent`, `status = :newStatus`, `total_learn_sec = :newTotalLearn`, `updated_at = NOW()`.

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác tính toán lại có tính idempotent (kết quả tính lại trên cùng tập dữ liệu nguồn là bất biến).
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi bước tính toán và cập nhật.
- **Retry Strategy**: Nếu tiến trình tính toán bị ngắt quãng, worker hoặc client có thể phát lại yêu cầu recalculate an toàn.
- **Audit Logging**: Ghi nhật ký vết khi có hành động tính toán lại thủ công từ phía admin/instructor.
- **Monitoring**: Theo dõi độ sâu cây khi bubble-up và thời gian thực thi tính toán lại.
- **Metrics**: `result_service_progress_recalculate_duration_seconds`, `result_service_progress_recalculate_nodes_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) xuyên suốt quá trình duyệt cây bubble-up.
