# API Blueprint — Lấy danh sách lịch sử nộp bài

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Lấy danh sách lịch sử nộp bài API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Attempt History List Query
- **Description**: Truy vấn danh sách lịch sử nộp bài học viên (`user_content_attempts`) có phân trang, hỗ trợ lọc theo learner (`userId`), bài tập/đề thi (`contentId`), loại đối tượng (`contentType`), node tiến độ (`nodeId`), trạng thái nộp bài (`status`) và trạng thái chấm điểm (`gradingStatus`).
- **Related Tables**: `user_content_attempts`
- **Related Services**: `lms-content-builder`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `GET`
- **URL**: `/api/v1/result-service/content-attempts`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant sở hữu | Format UUID v4 |

#### Path Variables

None.

#### Query Parameters

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `userId` | String | No | Lọc theo ID học viên | Format UUID v4 |
| `contentId` | String | No | Lọc theo ID bài tập/đề thi | Format UUID v4 |
| `contentType` | String | No | Lọc theo loại đối tượng | Enum: `ASSIGNMENT`, `EXAM`, `QUIZ`, `QUESTION` |
| `nodeId` | String | No | Lọc theo node tiến độ trong cây | Format UUID v4 |
| `status` | String | No | Lọc theo trạng thái nộp | Enum: `draft`, `in_progress`, `submitted`, `auto_submitted`, `locked`, `voided` |
| `gradingStatus` | String | No | Lọc theo trạng thái chấm | Enum: `pending`, `grading`, `graded`, `returned` |
| `page` | Integer | No | Trang hiện tại | Integer >= 1, Mặc định: 1 |
| `size` | Integer | No | Kích thước trang | Integer 1–100, Mặc định: 20 |
| `sort` | String | No | Trường sắp xếp | Mặc định: `startedAt,desc` |

#### Request Body

None.

---

### Response

- **Success Status**: `200 OK`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `items` | Array of Objects | Danh sách bản ghi tóm tắt các lần nộp bài |
| `items[].id` | String | ID lượt nộp bài (`user_content_attempts.id`) |
| `items[].tenantId` | String | ID tenant |
| `items[].userId` | String | ID học viên |
| `items[].nodeId` | String | ID node vị trí tiến độ |
| `items[].contentId` | String | ID đối tượng nội dung |
| `items[].contentType` | String | Loại đối tượng nội dung |
| `items[].seqNo` | Integer | Số thứ tự lượt nộp (1-based) |
| `items[].startedAt` | String | Thời điểm bắt đầu (ISO-8601 UTC) |
| `items[].submittedAt` | String | Thời điểm nộp bài (ISO-8601 UTC) |
| `items[].status` | String | Trạng thái nộp bài |
| `items[].gradingStatus` | String | Trạng thái chấm điểm |
| `items[].score` | Decimal | Điểm thô |
| `items[].maxScore` | Decimal | Điểm tối đa |
| `items[].penaltyScore` | Decimal | Điểm trừ |
| `items[].finalScore` | Decimal | Điểm cuối cùng |
| `items[].isPassed` | Boolean | Trạng thái Đạt / Không đạt |
| `items[].isLate` | Boolean | Đánh dấu nộp trễ |
| `items[].timeTakenSec` | Integer | Thời gian làm thực tế (giây) |
| `page` | Integer | Số trang |
| `size` | Integer | Kích thước trang |
| `total` | Long | Tổng số bản ghi thỏa điều kiện |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Query parameter không hợp lệ | Malformed or out of range query parameters. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền xem danh sách lịch sử này | Access denied to specified attempt history resources. |
| `RES-500-001` | 500 | Lỗi hệ thống khi truy vấn | An unexpected error occurred while listing attempt records. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Kiểm tra HTTP Header `X-Tenant-Id`. Trả về `RES-401-001` nếu thiếu.
2. Đọc và kiểm tra các tham số Query Parameters (`userId`, `contentId`, `contentType`, `status`, `gradingStatus`, `page`, `size`, `sort`). Trả về `RES-400-001` nếu tham số không hợp lệ.
3. Delegate gọi Service layer `ListAttemptHistoryService`.
4. Wrap danh sách kết quả vào paginated response DTO và trả về phản hồi HTTP `200 OK`.

---

### Service Layer

1. Kiểm tra phân quyền truy cập:
   - Học viên thông thường chỉ được xem lịch sử làm bài của chính mình (`userId` trùng khớp caller).
   - Giảng viên/Quản trị viên (`ROLE_INSTRUCTOR`, `ROLE_ADMIN`) được phép xem danh sách bài làm của học viên khác trong cùng tenant.
   - Nếu vi phạm phân quyền, ném lỗi `RES-403-001`.
2. Gọi Repository layer thực thi query tìm kiếm có phân trang.
3. Đếm tổng số bản ghi thỏa điều kiện để tính `total` số trang.
4. Trả về cấu hình đối tượng danh sách kết quả.

---

### Repository Layer

1. Xây dựng câu truy vấn động dựa trên bộ lọc: `tenant_id = :tenantId`, `user_id`, `content_id`, `content_type`, `node_id`, `status`, `grading_status`.
2. Áp dụng sắp xếp theo `started_at DESC` và pagination limit/offset.

---

### External Interaction

None.

---

### Validation

#### Request Validation

- `X-Tenant-Id` phải là UUID v4 hợp lệ.
- `userId`, `contentId`, `nodeId` phải là UUID v4 hợp lệ nếu được truyền.
- `page` >= 1, `size` trong khoảng từ 1 đến 100.
- `contentType`, `status`, `gradingStatus` phải thuộc danh sách Enum tương ứng nếu được truyền.

#### Business Validation

- Mọi truy vấn bắt buộc phải gắn phạm vi `tenant_id`.

#### Permission Validation

- Caller không có vai trò quản trị/giảng viên bắt buộc phải lọc theo đúng `userId` của mình.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn danh sách lịch sử nộp bài có lọc và phân trang

- **Operation Type**: `SELECT`
- **Target Table**: `user_content_attempts`
- **Conditions**: `tenant_id = :tenantId AND (:userId IS NULL OR user_id = :userId) AND (:contentId IS NULL OR content_id = :contentId) AND (:contentType IS NULL OR content_type = :contentType) AND (:nodeId IS NULL OR node_id = :nodeId) AND (:status IS NULL OR status = :status) AND (:gradingStatus IS NULL OR grading_status = :gradingStatus)`
- **Expected Result**: Trả về mảng danh sách bản ghi tóm tắt sắp xếp theo `started_at DESC` với LIMIT `:size` OFFSET `(:page - 1) * :size`.
- **Performance Notes**: Tận dụng các chỉ mục hỗ trợ:
  - `ix_uca_user_content (tenant_id, user_id, content_id, started_at)`
  - `ix_uca_user_node_started (tenant_id, user_id, node_id, started_at)`
  - `ix_uca_content (tenant_id, content_type, content_id, started_at)`
  - `ix_uca_grading_status (tenant_id, grading_status, submitted_at)`

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác truy vấn danh sách (`GET`), có tính chất idempotent tuyệt đối.
- **Tenant Isolation**: Bắt buộc lọc theo `tenant_id` trong mọi truy vấn SQL.
- **Retry Strategy**: Client có thể tự động thử lại an toàn khi gặp sự cố ngắt kết nối mạng.
- **Audit Logging**: Không yêu cầu ghi log audit cho thao tác xem danh sách.
- **Monitoring**: Theo dõi thời gian phản hồi truy vấn danh sách bài nộp và tần suất lọc theo hàng đợi chấm (`gradingStatus = pending`).
- **Metrics**: `result_service_attempts_list_query_seconds`, `result_service_attempts_list_query_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) qua các tầng xử lý.
