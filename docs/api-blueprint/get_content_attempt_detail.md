# API Blueprint — Lấy chi tiết lượt nộp bài

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Lấy chi tiết lượt nộp bài API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Attempt Detail Query
- **Description**: Truy vấn thông tin chi tiết một lượt nộp bài cụ thể (`user_content_attempts`) của học viên theo ID, bao gồm trạng thái làm bài, trạng thái chấm điểm, điểm thô, điểm trừ, điểm final, danh sách câu trả lời kèm kết quả từng câu, và dữ liệu giám sát thi (proctoring).
- **Related Tables**: `user_content_attempts`
- **Related Services**: `lms-content-builder` (Tham chiếu cấu trúc đề thi/câu hỏi)

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `GET`
- **URL**: `/api/v1/result-service/content-attempts/{id}`
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
| `id` | String | Yes | ID lượt nộp bài (`user_content_attempts.id`) | Format UUID v4 |

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
| `id` | String | ID lượt nộp bài (`user_content_attempts.id`) |
| `tenantId` | String | ID tenant sở hữu |
| `userId` | String | ID learner sở hữu lần nộp |
| `nodeId` | String | ID node vị trí trong cây tiến độ (`user_content_results.id`) |
| `contentId` | String | ID bài tập / đề thi / quiz |
| `contentType` | String | Loại bài (`ASSIGNMENT`, `EXAM`, `QUIZ`, `QUESTION`) |
| `seqNo` | Integer | Số thứ tự lần nộp bài (1-based sequence number) |
| `startedAt` | String | Thời điểm bắt đầu làm bài (ISO-8601 UTC) |
| `endedAt` | String | Thời điểm kết thúc phiên làm (ISO-8601 UTC) |
| `submittedAt` | String | Thời điểm nộp bài chủ động (ISO-8601 UTC) |
| `autoSubmittedAt` | String | Thời điểm hệ thống tự nộp khi hết giờ (ISO-8601 UTC) |
| `gradedAt` | String | Thời điểm hoàn thành chấm điểm (ISO-8601 UTC) |
| `status` | String | Vòng đời nộp (`draft`, `in_progress`, `submitted`, `auto_submitted`, `locked`, `voided`) |
| `gradingStatus` | String | Vòng đời chấm (`pending`, `grading`, `graded`, `returned`) |
| `score` | Decimal | Điểm thô đạt được |
| `maxScore` | Decimal | Điểm tối đa |
| `penaltyScore` | Decimal | Điểm bị trừ (nộp trễ, dùng gợi ý) |
| `finalScore` | Decimal | Điểm cuối cùng = score - penaltyScore |
| `isPassed` | Boolean | Kết quả Đạt / Không đạt |
| `isLate` | Boolean | Cờ đánh dấu nộp trễ hạn |
| `graderId` | String | ID người chấm (NULL nếu chấm tự động) |
| `timeTakenSec` | Integer | Thời gian làm bài thực tế (giây) |
| `durationSec` | Integer | Tổng thời gian phiên (giây) |
| `deviceKind` | String | Loại thiết bị (`desktop`, `mobile`, `tablet`) |
| `ipAddress` | String | IP client lúc làm bài |
| `sessionId` | String | Session ID làm bài |
| `endedReason` | String | Lý do kết thúc (`submitted`, `timeout`, `voided`, `system_error`) |
| `contentVersionId` | String | Version đề thi/câu hỏi lúc làm bài |
| `sourceService` | String | Service phát sinh lần nộp |
| `sourceRef` | String | ID tham chiếu bên dịch vụ nguồn |
| `answers` | Array of Objects | Danh sách câu trả lời per-question kèm kết quả chấm |
| `answers[].questionId` | String | ID câu hỏi |
| `answers[].questionVersionId` | String | Snapshot version câu hỏi lúc làm |
| `answers[].answer` | Object | Nội dung câu trả lời của learner |
| `answers[].isCorrect` | Boolean | Kết quả Đúng/Sai của câu (NULL nếu chưa chấm) |
| `answers[].score` | Decimal | Điểm đạt được của câu (NULL nếu chưa chấm) |
| `answers[].maxScore` | Decimal | Điểm tối đa của câu |
| `proctoringData` | Object | Dữ liệu giám sát thi (chuyển tab, snapshot, webcam) |
| `createdAt` | String | Thời điểm tạo bản ghi lần nộp |
| `updatedAt` | String | Thời điểm cập nhật gần nhất |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Path variable `id` không hợp lệ | Malformed request parameters. Invalid UUID format for attempt ID. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền truy cập lượt nộp bài này | Access denied for the specified attempt record. |
| `RES-404-001` | 404 | Không tìm thấy lượt nộp bài | Attempt record not found for the specified ID and tenant. |
| `RES-500-001` | 500 | Lỗi hệ thống khi truy vấn cơ sở dữ liệu | An unexpected error occurred while fetching attempt details. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Đọc và kiểm tra HTTP Header `X-Tenant-Id`. Trả về `RES-401-001` nếu thiếu.
2. Kiểm tra tính hợp lệ của Path variable `id` (phải là chuỗi UUID v4 hợp lệ). Trả về `RES-400-001` nếu sai định dạng.
3. Gọi Service layer `GetAttemptDetailService`.
4. Chuyển đổi Domain Model kết quả thành Response DTO và trả về phản hồi HTTP `200 OK`.

---

### Service Layer

1. Truy vấn bản ghi nộp bài từ Repository theo `(tenantId, id)`.
2. Nếu không tìm thấy bản ghi, ném lỗi nghiệp vụ `AttemptNotFoundException` tương ứng HTTP `404 Not Found` (`RES-404-001`).
3. Kiểm tra phân quyền truy cập:
   - Cho phép nếu caller user ID trùng với `userId` sở hữu lượt nộp bài.
   - Cho phép nếu caller sở hữu vai trò Quản trị viên/Giảng viên (`ROLE_ADMIN`, `ROLE_INSTRUCTOR`) thuộc cùng tenant.
   - Nếu không thỏa mãn, ném lỗi `RES-403-001`.
4. Trả về đối tượng chi tiết lần nộp bài.

---

### Repository Layer

1. Thực thi truy vấn SELECT theo khóa chính `id` kết hợp bộ lọc `tenant_id = :tenantId` trên bảng `user_content_attempts`.

---

### External Interaction

None. (Thao tác đọc thuần túy, có thể truy xuất thêm Cache từ Redis nếu khả dụng).

---

### Validation

#### Request Validation

- `X-Tenant-Id` phải là UUID v4 hợp lệ.
- Path parameter `id` phải là UUID v4 hợp lệ.

#### Business Validation

- Bản ghi phải tồn tại và thuộc đúng `tenant_id` được chỉ định.

#### Permission Validation

- Caller chỉ được xem lượt nộp bài của chính mình hoặc phải có quyền giám sát/giảng dạy trong tenant.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn bản ghi chi tiết lần nộp bài

- **Operation Type**: `SELECT`
- **Target Table**: `user_content_attempts`
- **Conditions**: `tenant_id = :tenantId AND id = :id`
- **Expected Result**: Trả về 1 bản ghi duy nhất chứa toàn bộ trường chi tiết lượt nộp bài.
- **Performance Notes**: Tận dụng tìm kiếm trực tiếp trên khóa chính `pk_user_content_attempts (id)`.

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác đọc thuần túy (`GET`), có tính chất idempotent tuyệt đối.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong truy vấn cơ sở dữ liệu.
- **Retry Strategy**: Client có thể tự động thử lại an toàn khi gặp sự cố ngắt kết nối mạng.
- **Audit Logging**: Không yêu cầu ghi nhật ký vết truy cập đọc thông thường.
- **Monitoring**: Theo dõi thời gian phản hồi truy vấn chi tiết lượt nộp bài và tỷ lệ lỗi 404/403.
- **Metrics**: `result_service_attempt_detail_query_seconds`, `result_service_attempt_detail_query_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) từ API Controller đến Database Driver.
