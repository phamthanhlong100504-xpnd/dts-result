# API Blueprint — Ghi nhận nhật ký học tập

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Ghi nhận nhật ký học tập API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Passive Learning Log Recording
- **Description**: Tiếp nhận và lưu trữ nhật ký các phiên học tập bị động (`learning_logs`) của học viên (xem video, nghe audio, đọc tài liệu PDF/slide). Tự động cập nhật cộng dồn thời gian học (`total_learn_sec`) và số phiên học (`learn_count`) cho node tiến độ liên quan trong `user_content_results`.
- **Related Tables**: `learning_logs`, `user_content_results`
- **Related Services**: `lms-content-builder` (Tham chiếu media/tài liệu)

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `POST`
- **URL**: `/api/v1/result-service/learning-logs`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant sở hữu | Format UUID v4 |
| `X-User-Id` | String | Yes | ID học viên thực hiện học | Format UUID v4 |

#### Path Variables

None.

#### Query Parameters

None.

#### Request Body

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `nodeId` | String | No | ID vị trí node trong cây tiến độ (`user_content_results.id`) | Format UUID v4 nếu có |
| `contentId` | String | Yes | ID đối tượng nội dung học (video, document) | Format UUID v4 |
| `contentType` | String | Yes | Loại đối tượng nội dung | Enum: `VIDEO`, `AUDIO`, `DOCUMENT`, `LESSON` |
| `sessionKind` | String | Yes | Loại phiên học bị động | Enum: `MEDIA_VIEW`, `DOCUMENT_READ` |
| `startedAt` | String | Yes | Thời điểm bắt đầu phiên xem/đọc | Format ISO-8601 UTC |
| `endedAt` | String | Yes | Thời điểm kết thúc phiên xem/đọc | Format ISO-8601 UTC, `endedAt` >= `startedAt` |
| `durationSec` | Integer | Yes | Tổng thời gian xem/đọc trong phiên (giây) | Integer >= 0 |
| `mediaPositionSec` | Integer | No | Vị trí dừng của video/audio (giây) | Integer >= 0 nếu có |
| `documentPageRead` | Integer | No | Số trang tài liệu đã đọc trong phiên | Integer >= 0 nếu có |
| `deviceKind` | String | No | Loại thiết bị sử dụng | Enum: `desktop`, `mobile`, `tablet` |
| `metadata` | Object | No | Dữ liệu phụ trợ (tốc độ phát, độ phân giải) | JSON Object |

---

### Response

- **Success Status**: `201 Created`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `id` | String | ID bản ghi nhật ký học tập (`learning_logs.id`) |
| `tenantId` | String | ID tenant |
| `userId` | String | ID học viên |
| `contentId` | String | ID đối tượng nội dung |
| `sessionKind` | String | Loại phiên học bị động |
| `durationSec` | Integer | Thời gian học được ghi nhận (giây) |
| `startedAt` | String | Thời điểm bắt đầu |
| `endedAt` | String | Thời điểm kết thúc |
| `createdAt` | String | Thời điểm tạo bản ghi nhật ký |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Dữ liệu nhật ký học tập không hợp lệ | Malformed learning log payload or invalid parameter formats. |
| `RES-400-002` | 400 | Mốc thời gian kết thúc nhỏ hơn thời gian bắt đầu | Invalid session timeframe: endedAt cannot be earlier than startedAt. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền ghi nhật ký học | Access denied for recording learning log. |
| `RES-404-001` | 404 | Không tìm thấy đối tượng nội dung hoặc node | Targeted content or progress node not found. |
| `RES-500-001` | 500 | Lỗi lưu trữ cơ sở dữ liệu | An unexpected error occurred while saving learning log. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Đọc và kiểm tra HTTP Headers `X-Tenant-Id` và `X-User-Id`. Trả về `RES-401-001` nếu thiếu.
2. Kiểm tra tính hợp lệ của Request Body (kiểm tra Enum, `durationSec` >= 0, `endedAt` >= `startedAt`). Trả về `RES-400-001` hoặc `RES-400-002` nếu lỗi.
3. Delegate gọi Service layer `RecordLearningLogService`.
4. Trả về thông tin nhật ký vừa tạo kèm HTTP status `201 Created`.

---

### Service Layer

1. Xác thực quyền hạn: Học viên ghi nhận nhật ký cho chính `userId` của mình trong tenant.
2. Lưu bản ghi nhật ký học bị động mới vào bảng `learning_logs`.
3. Nếu request có kèm `nodeId`, thực hiện cập nhật cộng dồn tiến độ học tập trên `user_content_results`:
   - Cộng dồn `total_learn_sec = total_learn_sec + durationSec`.
   - Tăng `learn_count = learn_count + 1`.
   - Cập nhật `last_activity_at = endedAt` và `updated_at = NOW()`.
4. Phát sự kiện Outbox `learning_log.recorded` để worker đồng bộ hạ tầng báo cáo.

---

### Repository Layer

1. Thực thi chèn 1 bản ghi mới vào bảng `learning_logs`.
2. Thực thi cập nhật `user_content_results` cho node tiến độ liên quan.

---

### External Interaction

- **Kafka**: Đẩy sự kiện tới topic `lms.result.learning-log-recorded`.

---

### Validation

#### Request Validation

- `X-Tenant-Id`, `X-User-Id`, `contentId` phải là UUID v4 hợp lệ.
- `contentType` thuộc Enum (`VIDEO`, `AUDIO`, `DOCUMENT`, `LESSON`).
- `sessionKind` thuộc Enum (`MEDIA_VIEW`, `DOCUMENT_READ`).
- `durationSec` >= 0 và `startedAt` <= `endedAt`.

#### Business Validation

- Thời gian phiên học không được vượt quá khoảng thời gian thực tế chênh lệch giữa `startedAt` và `endedAt`.

#### Permission Validation

- Caller phải trùng khớp với `X-User-Id`.

---

## Part 3 — Data Interaction

### Operation 1: Chèn bản ghi nhật ký học tập mới

- **Operation Type**: `INSERT`
- **Target Table**: `learning_logs`
- **Conditions**: Khóa chính `id` tự sinh bằng UUID v7.
- **Expected Result**: Chèn thành công 1 bản ghi nhật ký học tập bị động mới.

---

### Operation 2: Cập nhật chỉ số cộng dồn cho node tiến độ

- **Operation Type**: `UPDATE`
- **Target Table**: `user_content_results`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND id = :nodeId`
- **Expected Result**: Cập nhật `total_learn_sec = total_learn_sec + :durationSec`, `learn_count = learn_count + 1`, `last_activity_at = :endedAt`, `updated_at = NOW()`.

---

## Part 4 — Operational Notes

- **Idempotency**: Ghi nhật ký học bị động có tính chất lũy tiến; client có thể dùng session ID để ngăn cộng lặp khi retry.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi thao tác lưu trữ và cập nhật SQL.
- **Retry Strategy**: Client có thể tự động thử lại khi gặp ngắt kết nối mạng.
- **Audit Logging**: Bản ghi `learning_logs` đóng vai trò lưu vết nhật ký học bị động của học viên.
- **Monitoring**: Theo dõi tổng thời gian học trung bình mỗi phiên và tần suất ghi nhật ký bị động.
- **Metrics**: `result_service_learning_logs_recorded_total`, `result_service_learning_duration_seconds_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) xuyên suốt các tầng.
