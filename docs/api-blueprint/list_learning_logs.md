# API Blueprint — Lấy danh sách nhật ký học

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Lấy danh sách nhật ký học API
- **API Type**: Public / Internal
- **Module**: Result Management
- **Feature**: Passive Learning Log Query
- **Description**: Truy vấn danh sách nhật ký các phiên học tập bị động (`learning_logs`) của học viên có phân trang, hỗ trợ lọc theo learner (`userId`), nội dung học (`contentId`), loại nội dung (`contentType`), và loại phiên (`sessionKind`).
- **Related Tables**: `learning_logs`
- **Related Services**: `lms-content-builder`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `GET`
- **URL**: `/api/v1/result-service/learning-logs`
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
| `contentId` | String | No | Lọc theo ID nội dung học | Format UUID v4 |
| `contentType` | String | No | Lọc theo loại đối tượng | Enum: `VIDEO`, `AUDIO`, `DOCUMENT`, `LESSON` |
| `sessionKind` | String | No | Lọc theo loại phiên học | Enum: `MEDIA_VIEW`, `DOCUMENT_READ` |
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
| `items` | Array of Objects | Danh sách các bản ghi nhật ký học tập |
| `items[].id` | String | ID bản ghi nhật ký (`learning_logs.id`) |
| `items[].tenantId` | String | ID tenant |
| `items[].userId` | String | ID học viên |
| `items[].nodeId` | String | ID node tiến độ liên quan |
| `items[].contentId` | String | ID đối tượng nội dung |
| `items[].contentType` | String | Loại đối tượng nội dung |
| `items[].sessionKind` | String | Loại phiên học bị động |
| `items[].startedAt` | String | Thời điểm bắt đầu phiên xem/đọc (ISO-8601 UTC) |
| `items[].endedAt` | String | Thời điểm kết thúc phiên xem/đọc (ISO-8601 UTC) |
| `items[].durationSec` | Integer | Thời gian học trong phiên (giây) |
| `items[].mediaPositionSec` | Integer | Vị trí dừng media (giây, nếu có) |
| `items[].documentPageRead` | Integer | Số trang tài liệu đã đọc (nếu có) |
| `items[].deviceKind` | String | Loại thiết bị |
| `items[].createdAt` | String | Thời điểm tạo nhật ký |
| `page` | Integer | Số trang hiện tại |
| `size` | Integer | Kích thước trang |
| `total` | Long | Tổng số bản ghi thỏa điều kiện |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Query parameter không hợp lệ | Malformed or out of range query parameters. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền truy vấn danh sách nhật ký học này | Access denied to learning log records. |
| `RES-500-001` | 500 | Lỗi hệ thống khi truy vấn | An unexpected error occurred while listing learning logs. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Kiểm tra HTTP Header `X-Tenant-Id`. Trả về `RES-401-001` nếu thiếu.
2. Đọc và kiểm tra các tham số Query Parameters (`userId`, `contentId`, `contentType`, `sessionKind`, `page`, `size`, `sort`). Trả về `RES-400-001` nếu tham số lỗi.
3. Delegate gọi Service layer `ListLearningLogsService`.
4. Wrap kết quả thành paginated response DTO và trả về HTTP status `200 OK`.

---

### Service Layer

1. Kiểm tra quyền truy cập: Học viên thông thường chỉ xem được nhật ký học của chính mình. Giảng viên/Admin xem được nhật ký học viên thuộc cùng tenant.
2. Gọi Repository layer thực thi câu truy vấn tìm kiếm nhật ký có phân trang.
3. Đếm tổng số bản ghi thỏa mãn để trả về `total`.
4. Trả về đối tượng kết quả phân trang.

---

### Repository Layer

1. Xây dựng câu truy vấn động lọc theo: `tenant_id = :tenantId`, `user_id`, `content_id`, `content_type`, `session_kind`.
2. Áp dụng sắp xếp theo `started_at DESC` và pagination limit/offset.

---

### External Interaction

None.

---

### Validation

#### Request Validation

- `X-Tenant-Id` phải là UUID v4 hợp lệ.
- `userId`, `contentId` phải là UUID v4 hợp lệ nếu được truyền.
- `page` >= 1, `size` trong khoảng từ 1 đến 100.

#### Business Validation

- Mọi câu lệnh SQL đều bắt buộc giới hạn trong phạm vi `tenant_id`.

#### Permission Validation

- Caller không phải admin/instructor phải gắn điều kiện `userId` trùng khớp với mình.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn danh sách nhật ký học tập bị động

- **Operation Type**: `SELECT`
- **Target Table**: `learning_logs`
- **Conditions**: `tenant_id = :tenantId AND (:userId IS NULL OR user_id = :userId) AND (:contentId IS NULL OR content_id = :contentId) AND (:contentType IS NULL OR content_type = :contentType) AND (:sessionKind IS NULL OR session_kind = :sessionKind)`
- **Expected Result**: Trả về mảng danh sách bản ghi nhật ký học tập bị động sắp xếp theo `started_at DESC` với LIMIT `:size` OFFSET `(:page - 1) * :size`.

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác truy vấn danh sách (`GET`), có tính chất idempotent tuyệt đối.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi truy vấn SQL.
- **Retry Strategy**: Client có thể tự động thử lại an toàn khi gặp sự cố kết nối mạng.
- **Audit Logging**: Không yêu cầu ghi log audit cho thao tác xem danh sách nhật ký.
- **Monitoring**: Theo dõi thời gian phản hồi truy vấn nhật ký học tập bị động.
- **Metrics**: `result_service_learning_logs_list_query_seconds`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) qua các tầng.
