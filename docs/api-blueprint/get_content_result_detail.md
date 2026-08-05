# API Blueprint — Lấy chi tiết node tiến độ

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Lấy chi tiết node tiến độ API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Content Progress Node Detail Query
- **Description**: Truy vấn thông tin chi tiết của một node tiến độ học tập cụ thể (`user_content_results`) theo ID, bao gồm trạng thái hoàn thành, phần trăm, tổng thời gian học, số lượt học/xem, số lần nộp bài, điểm cao nhất/gần nhất, và mảng câu trả lời (đối với leaf node).
- **Related Tables**: `user_content_results`
- **Related Services**: `lms-content-builder`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `GET`
- **URL**: `/api/v1/result-service/content-results/{id}`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant sở hữu | Format UUID v4 |

#### Path Variables

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `id` | String | Yes | ID node tiến độ (`user_content_results.id`) | Format UUID v4 |

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
| `id` | String | ID node tiến độ (`user_content_results.id`) |
| `tenantId` | String | ID tenant sở hữu |
| `userId` | String | ID học viên sở hữu |
| `contentId` | String | ID đối tượng nội dung |
| `contentType` | String | Loại đối tượng nội dung (`videos`, `lessons`, `coursewares`) |
| `contentVersionId` | String | Version nội dung đang học |
| `parentNodeId` | String | ID node cha trực tiếp (NULL nếu là gốc) |
| `contentCode` | String | Materialized path (ví dụ: `UUID1:UUID2:UUID3`) |
| `status` | String | Trạng thái (`NOT_COMPLETED`, `COMPLETED`) |
| `percent` | Integer | Phần trăm hoàn thành (0–100) |
| `totalLearnSec` | Integer | Tổng thời gian học bị động (giây) |
| `learnCount` | Integer | Số lần mở xem/đọc nội dung |
| `attemptCount` | Integer | Số lần nộp/trả lời bài tập |
| `bestScore` | Decimal | Điểm cao nhất đạt được |
| `lastScore` | Decimal | Điểm lần nộp gần nhất |
| `lastScoreAt` | String | Thời điểm chấm/nộp gần nhất (ISO-8601 UTC) |
| `lastAttemptId` | String | ID lần nộp gần nhất |
| `answers` | Array of Objects | Mảng câu trả lời per-question cho leaf node 1 lần (nếu có) |
| `metadata` | Object | Dữ liệu đặc thù từng loại node (resume video, trang tài liệu) |
| `startedAt` | String | Thời điểm bắt đầu học lần đầu (ISO-8601 UTC) |
| `lastActivityAt` | String | Thời điểm hoạt động gần nhất (ISO-8601 UTC) |
| `completedAt` | String | Thời điểm hoàn thành (ISO-8601 UTC) |
| `createdAt` | String | Thời điểm tạo bản ghi lần đầu |
| `updatedAt` | String | Thời điểm worker tính lại gần nhất |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Path variable `id` không hợp lệ | Malformed request parameters. Invalid UUID format. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền xem node tiến độ này | Access denied to specified content result resource. |
| `RES-404-001` | 404 | Không tìm thấy node tiến độ | Content result node not found for specified ID and tenant. |
| `RES-500-001` | 500 | Lỗi hệ thống khi truy vấn | An unexpected error occurred while fetching content result node. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Kiểm tra HTTP Header `X-Tenant-Id` và Path variable `id`. Trả về `RES-401-001` hoặc `RES-400-001` nếu thiếu/sai định dạng.
2. Delegate gọi Service layer `GetContentResultDetailService`.
3. Map Domain Model thành Response DTO và trả về HTTP `200 OK`.

---

### Service Layer

1. Truy vấn bản ghi `user_content_results` từ Repository theo `(tenantId, id)`.
2. Ném lỗi `RES-404-001` nếu bản ghi không tồn tại.
3. Kiểm tra quyền hạn truy cập: Caller phải là sở hữu của bản ghi (`userId`) hoặc giữ vai trò Quản trị viên/Giảng viên thuộc tenant. Nếu không thỏa mãn, ném lỗi `RES-403-001`.
4. Trả về chi tiết node tiến độ.

---

### Repository Layer

1. Thực thi câu truy vấn SELECT theo khóa chính `id` kết hợp điều kiện `tenant_id = :tenantId` trên bảng `user_content_results`.

---

### External Interaction

None.

---

### Validation

#### Request Validation

- `X-Tenant-Id` và `id` phải là UUID v4 hợp lệ.

#### Business Validation

- Bản ghi node phải tồn tại và thuộc phạm vi `tenant_id` chỉ định.

#### Permission Validation

- Caller chỉ được xem node tiến độ của chính mình trừ khi giữ vai trò quản trị/giảng viên.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn chi tiết node tiến độ học tập

- **Operation Type**: `SELECT`
- **Target Table**: `user_content_results`
- **Conditions**: `tenant_id = :tenantId AND id = :id`
- **Expected Result**: Trả về 1 bản ghi duy nhất chứa thông tin chi tiết node tiến độ.
- **Performance Notes**: Tận dụng tìm kiếm theo khóa chính `pk_user_content_results (id)`.

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác đọc (`GET`), có tính chất idempotent tuyệt đối.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong truy vấn SQL.
- **Retry Strategy**: Client có thể tự động thử lại an toàn khi gặp sự cố kết nối.
- **Audit Logging**: Không yêu cầu ghi log audit cho thao tác đọc chi tiết node.
- **Monitoring**: Theo dõi thời gian phản hồi truy vấn chi tiết node tiến độ.
- **Metrics**: `result_service_content_result_detail_query_seconds`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) qua các lớp xử lý.
