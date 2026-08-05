# API Blueprint — Lấy cây tiến độ học tập

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Lấy cây tiến độ học tập API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Progress Tree Query
- **Description**: Truy vấn cây tiến độ học tập (`user_content_results`) dạng danh sách các node tiến độ (trạng thái, phần trăm hoàn thành, tổng thời gian học, số lượt học/xem, số lần nộp bài, điểm cao nhất, điểm gần nhất) của một học viên theo `userId`. Hỗ trợ lọc theo nhánh cây (`contentCode` prefix scan) hoặc trạng thái hoàn thành.
- **Related Tables**: `user_content_results`
- **Related Services**: `lms-content-builder` (Tham chiếu cấu trúc đối tượng nội dung)

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `GET`
- **URL**: `/api/v1/result-service/users/{userId}/content-results`
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
| `userId` | String | Yes | ID học viên cần lấy tiến độ | Format UUID v4 |

#### Query Parameters

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `contentCode` | String | No | Materialized path prefix để lọc nhánh cây | Chuỗi văn bản (ví dụ: `UUID1:UUID2%`) |
| `status` | String | No | Lọc theo trạng thái hoàn thành | Enum: `NOT_COMPLETED`, `COMPLETED` |
| `page` | Integer | No | Số trang pagination | Integer >= 1, Mặc định: 1 |
| `size` | Integer | No | Kích thước trang | Integer 1–100, Mặc định: 50 |
| `sort` | String | No | Trường sắp xếp | Mặc định: `lastActivityAt,desc` |

#### Request Body

None.

---

### Response

- **Success Status**: `200 OK`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `items` | Array of Objects | Danh sách các node tiến độ học tập |
| `items[].id` | String | ID node tiến độ (`user_content_results.id`) |
| `items[].tenantId` | String | ID tenant sở hữu |
| `items[].userId` | String | ID học viên sở hữu |
| `items[].contentId` | String | ID đối tượng nội dung |
| `items[].contentType` | String | Tên bảng/loại nội dung (`videos`, `lessons`, `coursewares`) |
| `items[].contentVersionId` | String | Version nội dung đang học (nếu có) |
| `items[].parentNodeId` | String | ID node cha trực tiếp (NULL nếu là node gốc) |
| `items[].contentCode` | String | Materialized path (ví dụ: `UUID1:UUID2:UUID3`) |
| `items[].status` | String | Trạng thái (`NOT_COMPLETED`, `COMPLETED`) |
| `items[].percent` | Integer | Phần trăm hoàn thành node (0–100) |
| `items[].totalLearnSec` | Integer | Tổng thời gian học bị động (giây) |
| `items[].learnCount` | Integer | Số lần mở xem/đọc nội dung |
| `items[].attemptCount` | Integer | Số lần nộp/trả lời bài tập |
| `items[].bestScore` | Decimal | Điểm cao nhất đạt được qua các lần nộp |
| `items[].lastScore` | Decimal | Điểm lần nộp gần nhất |
| `items[].lastScoreAt` | String | Thời điểm chấm/nộp gần nhất (ISO-8601 UTC) |
| `items[].lastAttemptId` | String | ID lần nộp gần nhất (`user_content_attempts.id`) |
| `items[].answers` | Array of Objects | Mảng câu trả lời cho leaf node học 1 lần (nếu có) |
| `items[].startedAt` | String | Thời điểm bắt đầu học lần đầu (ISO-8601 UTC) |
| `items[].lastActivityAt` | String | Thời điểm hoạt động gần nhất (ISO-8601 UTC) |
| `items[].completedAt` | String | Thời điểm hoàn thành node (ISO-8601 UTC) |
| `items[].createdAt` | String | Thời điểm tạo bản ghi lần đầu |
| `items[].updatedAt` | String | Thời điểm worker tính toán lại gần nhất |
| `page` | Integer | Số trang hiện tại |
| `size` | Integer | Số lượng bản ghi mỗi trang |
| `total` | Long | Tổng số node thỏa điều kiện |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Path variable hoặc Query parameter không hợp lệ | Malformed parameters or invalid pagination range. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền truy cập tiến độ học viên này | Access denied to specified user progress resources. |
| `RES-404-001` | 404 | Không tìm thấy tiến độ học tập | Progress records not found for specified user and criteria. |
| `RES-500-001` | 500 | Lỗi hệ thống khi truy vấn dữ liệu | An unexpected error occurred while fetching progress tree. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Đọc và kiểm tra HTTP Header `X-Tenant-Id` và Path variable `userId`. Trả về `RES-401-001` hoặc `RES-400-001` nếu không hợp lệ.
2. Đọc các tham số Query parameters (`contentCode`, `status`, `page`, `size`, `sort`). Kiểm tra giới hạn phân trang.
3. Delegate gọi Service layer `GetUserProgressTreeService`.
4. Wrap kết quả thu được thành paginated response DTO và trả về HTTP `200 OK`.

---

### Service Layer

1. Kiểm tra quyền hạn truy cập:
   - Học viên chỉ được xem cây tiến độ của chính mình (`userId` trùng với caller user ID).
   - Giảng viên/Quản trị viên (`ROLE_INSTRUCTOR`, `ROLE_ADMIN`) có quyền xem tiến độ của học viên thuộc cùng tenant.
   - Nếu vi phạm, ném lỗi `RES-403-001`.
2. Gọi Repository layer để thực thi truy vấn lấy danh sách các node tiến độ thỏa điều kiện `tenant_id`, `user_id`, `content_code`, `status`.
3. Đếm tổng số bản ghi thỏa mãn để trả về thông tin phân trang `total`.
4. Trả về đối tượng cây tiến độ học tập.

---

### Repository Layer

1. Thực thi câu truy vấn SQL tìm danh sách các bản ghi trên bảng `user_content_results` theo `tenant_id = :tenantId AND user_id = :userId`.
2. Nếu tham số `contentCode` có giá trị, thêm điều kiện prefix scan: `content_code LIKE :contentCode || ':%' OR content_code = :contentCode`.
3. Nếu tham số `status` có giá trị, thêm điều kiện: `status = :status`.
4. Áp dụng sắp xếp `last_activity_at DESC` và pagination limit/offset.

---

### External Interaction

None. (Thao tác đọc dữ liệu tiến độ từ Read model DB / Redis Cache).

---

### Validation

#### Request Validation

- `X-Tenant-Id` và `userId` phải là UUID v4 hợp lệ.
- `page` >= 1, `size` trong khoảng từ 1 đến 100.
- `status` thuộc danh sách Enum (`NOT_COMPLETED`, `COMPLETED`) nếu được truyền.

#### Business Validation

- Mọi truy vấn phải bắt buộc gắn giới hạn `tenant_id` và `user_id`.

#### Permission Validation

- Caller không có quyền giảng viên/admin bắt buộc phải có `userId` khớp với `userId` trong đường dẫn URL.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn danh sách node tiến độ học tập của learner

- **Operation Type**: `SELECT`
- **Target Table**: `user_content_results`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND (:contentCode IS NULL OR content_code LIKE :contentCodePrefix) AND (:status IS NULL OR status = :status)`
- **Expected Result**: Trả về mảng các bản ghi tiến độ học tập dạng node sắp xếp theo `last_activity_at DESC` với LIMIT `:size` OFFSET `(:page - 1) * :size`.
- **Performance Notes**: Tận dụng các chỉ mục:
  - `ix_ucr_user_status_activity (tenant_id, user_id, status, last_activity_at)`
  - `ix_ucr_content_code (tenant_id, user_id, content_code text_pattern_ops)`

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác đọc thuần túy (`GET`), có tính chất idempotent tuyệt đối.
- **Tenant Isolation**: Bắt buộc chỉ định `tenant_id` trong mọi truy vấn cơ sở dữ liệu.
- **Retry Strategy**: Client có thể tự động thử lại an toàn khi gặp sự cố mạng.
- **Audit Logging**: Không yêu cầu ghi log audit cho thao tác đọc tiến độ.
- **Monitoring**: Theo dõi thời gian phản hồi truy vấn cây tiến độ và tần suất lọc theo prefix scan `content_code`.
- **Metrics**: `result_service_progress_tree_query_seconds`, `result_service_progress_tree_query_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) qua các lớp dịch vụ.
