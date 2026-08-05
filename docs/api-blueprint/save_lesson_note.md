# API Blueprint — Tạo / Cập nhật ghi chú

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Tạo / Cập nhật ghi chú API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Lesson Notes Management
- **Description**: Cho phép học viên tạo mới hoặc cập nhật ghi chú cá nhân (`lesson_notes`) trong quá trình xem video, đọc tài liệu hay tham gia bài học (lưu kèm mốc thời gian video `mediaTimestampSec` hoặc trang tài liệu).
- **Related Tables**: `lesson_notes`
- **Related Services**: `lms-content-builder`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `POST`
- **URL**: `/api/v1/result-service/notes`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant sở hữu | Format UUID v4 |
| `X-User-Id` | String | Yes | ID học viên tạo ghi chú | Format UUID v4 |

#### Path Variables

None.

#### Query Parameters

None.

#### Request Body

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `id` | String | No | ID ghi chú (Truyền khi cập nhật ghi chú cũ) | Format UUID v4 nếu có |
| `contentId` | String | Yes | ID bài học/nội dung được ghi chú | Format UUID v4 |
| `nodeId` | String | No | ID node vị trí tiến độ liên quan | Format UUID v4 nếu có |
| `contentVersionId` | String | No | Version bài học lúc tạo ghi chú | Format UUID v4 nếu có |
| `mediaTimestampSec` | Integer | No | Mốc thời gian video/audio được ghi chú (giây) | Integer >= 0 nếu có |
| `documentPage` | Integer | No | Trang tài liệu được ghi chú | Integer >= 1 nếu có |
| `noteText` | String | Yes | Nội dung văn bản ghi chú | Non-empty string, Max length 2000 |

---

### Response

- **Success Status**: `200 OK` (hoặc `201 Created` khi tạo mới)

#### Response Body

| Name | Type | Description |
|---|---|---|
| `id` | String | ID bản ghi ghi chú bài học (`lesson_notes.id`) |
| `tenantId` | String | ID tenant sở hữu |
| `userId` | String | ID học viên |
| `contentId` | String | ID đối tượng nội dung |
| `nodeId` | String | ID node tiến độ |
| `mediaTimestampSec` | Integer | Mốc thời gian media (nếu có) |
| `documentPage` | Integer | Trang tài liệu (nếu có) |
| `noteText` | String | Nội dung ghi chú đã lưu |
| `createdAt` | String | Thời điểm tạo bản ghi lần đầu (ISO-8601 UTC) |
| `updatedAt` | String | Thời điểm cập nhật bản ghi gần nhất (ISO-8601 UTC) |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Dữ liệu ghi chú không hợp lệ | Malformed note payload or note text exceeds maximum length. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền thao tác ghi chú của user khác | Access denied for lesson note modification. |
| `RES-404-001` | 404 | Không tìm thấy ghi chú chỉ định khi cập nhật | Lesson note not found for specified ID. |
| `RES-500-001` | 500 | Lỗi xử lý cơ sở dữ liệu hệ thống | An unexpected error occurred while saving lesson note. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Đọc và kiểm tra HTTP Headers `X-Tenant-Id` và `X-User-Id`. Trả về `RES-401-001` nếu thiếu.
2. Kiểm tra Request Body schema (`noteText` không rỗng, độ dài <= 2000 ký tự). Trả về `RES-400-001` nếu sai.
3. Delegate gọi Service layer `SaveLessonNoteService`.
4. Trả về thông tin ghi chú đã lưu kèm HTTP status `200 OK` (cập nhật) hoặc `201 Created` (tạo mới).

---

### Service Layer

1. Xác thực quyền hạn: Học viên thao tác ghi chú trên chính `userId` của mình.
2. **Trường hợp cập nhật** (Request chứa `id`):
   - Truy vấn bản ghi `lesson_notes` từ DB. Ném `RES-404-001` nếu không tìm thấy.
   - Kiểm tra `userId` trong DB phải trùng khớp với `X-User-Id`. Nếu vi phạm, ném lỗi `RES-403-001`.
   - Cập nhật `note_text`, `media_timestamp_sec`, `updated_at = NOW()`.
3. **Trường hợp tạo mới** (Request không chứa `id`):
   - Chèn bản ghi ghi chú mới vào bảng `lesson_notes`.
4. Trả về chi tiết bản ghi ghi chú vừa xử lý.

---

### Repository Layer

1. Thực thi `SELECT` kiểm tra tồn tại nếu cập nhật.
2. Thực thi `INSERT` bản ghi mới hoặc `UPDATE` bản ghi cũ dựa trên `id`.

---

### External Interaction

None.

---

### Validation

#### Request Validation

- `X-Tenant-Id`, `X-User-Id`, `contentId` phải là UUID v4 hợp lệ.
- `noteText` không được để trống và có độ dài tối đa 2000 ký tự.
- `mediaTimestampSec` >= 0 nếu được truyền.

#### Business Validation

- Người dùng không được phép chỉnh sửa ghi chú của học viên khác.

#### Permission Validation

- Caller phải khớp với `X-User-Id`.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn bản ghi ghi chú (trường hợp cập nhật)

- **Operation Type**: `SELECT`
- **Target Table**: `lesson_notes`
- **Conditions**: `tenant_id = :tenantId AND id = :id`
- **Expected Result**: Trả về thông tin bản ghi ghi chú bài học hiện tại.

---

### Operation 2: Chèn bản ghi ghi chú mới

- **Operation Type**: `INSERT`
- **Target Table**: `lesson_notes`
- **Conditions**: Khóa chính `id` tự sinh bằng UUID v4.
- **Expected Result**: Chèn thành công 1 bản ghi ghi chú bài học mới.

---

### Operation 3: Cập nhật nội dung ghi chú cũ

- **Operation Type**: `UPDATE`
- **Target Table**: `lesson_notes`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND id = :id`
- **Expected Result**: Cập nhật `note_text = :noteText`, `media_timestamp_sec = :mediaTimestampSec`, `updated_at = NOW()`.

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác cập nhật theo ID có tính chất idempotent.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi truy vấn SQL.
- **Retry Strategy**: Client có thể phát lại yêu cầu lưu an toàn.
- **Audit Logging**: Bản ghi `lesson_notes` lưu trữ đầy đủ `created_at` và `updated_at`.
- **Monitoring**: Theo dõi tổng số lượng ghi chú tạo mới của học viên.
- **Metrics**: `result_service_notes_saved_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) xuyên suốt các tầng.
