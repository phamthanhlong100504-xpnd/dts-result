# API Blueprint: Ghi nhận kết quả phiên học hoặc bài thi

## Part 0: API Identity

- **API Name**: Record Learning Result
- **API Type**: Internal / Public
- **Module**: Result & Tracking Service
- **Feature**: Learning Result Recording
- **Description**: Ghi nhận một lượt thực hiện (attempt) của phiên học, bài thi, hoặc bài tập. Tự động tính số thứ tự lần nộp (attempt_no) và cập nhật tiến độ tổng hợp (learning_summaries).
- **Related Tables**: `learning_results`, `learning_summaries`
- **Related Services**: `dts-identity`, `content-builder`

---

## Part 1: API Contract

### Endpoint
`POST /api/v1/result-service/learning-results`

### HTTP Method
`POST`

### Request Headers
| Header Key | Type | Required | Description |
|---|---|---|---|
| `X-User-Id` | UUID | Mandatory | ID người dùng thực hiện |
| `Content-Type` | String | Mandatory | `application/json` |

### Request Body
| Field Name | Type | Required | Validation Rules | Description |
|---|---|---|---|---|
| `sourceId` | UUID | Mandatory | Non-null | ID session gốc (phiên thi/thực hành) |
| `targetId` | UUID | Mandatory | Non-null | ID đối tượng học |
| `sourceType` | String | Mandatory | In (`EXAM_SESSION`, `PRACTICE_SESSION`, `LESSON_SESSION`, `ASSIGNMENT_SESSION`) | Loại session gốc |
| `targetType` | String | Mandatory | In (`EXAM`, `PRACTICE_SET`, `CHAPTER`, `LESSON`, `LEARNING_PROGRAM`) | Loại đối tượng |
| `score` | BigDecimal | Optional | >= 0 | Điểm đạt được |
| `maxScore` | BigDecimal | Optional | >= 0 | Điểm tối đa |
| `progress` | BigDecimal | Mandatory | 0..100 | Tỷ lệ % hoàn thành |
| `durationSeconds` | Integer | Mandatory | >= 0 | Thời gian thực hiện (giây) |
| `result` | String | Mandatory | In (`PASSED`, `FAILED`, `COMPLETED`, `ABANDONED`, `EXPIRED`) | Kết quả phiên |
| `startedAt` | OffsetDateTime | Mandatory | UTC, ISO-8601 | Thời điểm bắt đầu |
| `completedAt` | OffsetDateTime | Optional | >= startedAt | Thời điểm kết thúc |
| `resultSnapshot` | Map | Optional | Valid JSON Object | Snapshot chi tiết câu trả lời/điểm |
| `metadata` | Map | Optional | Valid JSON Object | Dữ liệu mở rộng |

### Response (201 Created)
| Field Name | Type | Description |
|---|---|---|
| `id` | UUID | ID kết quả được tạo |
| `userId` | UUID | ID người dùng |
| `sourceId` | UUID | ID session gốc |
| `targetId` | UUID | ID đối tượng học |
| `sourceType` | String | Loại session gốc |
| `targetType` | String | Loại đối tượng học |
| `attemptNo` | Integer | Số thứ tự lần thực hiện |
| `score` | BigDecimal | Điểm số |
| `maxScore` | BigDecimal | Điểm tối đa |
| `progress` | BigDecimal | Tỷ lệ hoàn thành % |
| `durationSeconds` | Integer | Thời gian học |
| `result` | String | Trạng thái kết quả |
| `startedAt` | OffsetDateTime | Thời điểm bắt đầu |
| `completedAt` | OffsetDateTime | Thời điểm kết thúc |
| `resultSnapshot` | Object | Snapshot kết quả |
| `metadata` | Object | Metadata |
| `createdAt` | OffsetDateTime | Thời điểm tạo |
| `updatedAt` | OffsetDateTime | Thời điểm cập nhật |

### Error Codes
| Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 Bad Request | Payload không hợp lệ | Malformed request parameters or invalid payload body. |
| `RES-500-001` | 500 Internal Server Error | Lỗi hệ thống | An unexpected error occurred while processing the request. |

---

## Part 2: Processing Spec

### Layer Responsibilities
- **Controller**: Trích xuất header `X-User-Id`, validate body request, gọi service `recordLearningResult`.
- **Service**: 
  - Kiểm tra tính duy nhất idempotency theo `(sourceId, sourceType)`.
  - Tính `attemptNo = max(attemptNo) + 1` cho `(userId, targetType, targetId)`.
  - Lưu bản ghi vào bảng `learning_results`.
  - Cập nhật/tạo mới tiến độ tổng hợp tại `learning_summaries` (cập nhật `attempt_count`, `completion_count`, `best_score`, `latest_score`, `average_score`, `progress`).
- **Repository**: Lưu trữ xuống PostgreSQL.

---

## Part 3: Data Interaction

- **SELECT**: `SELECT r FROM LearningResultEntity r WHERE r.sourceId = :sourceId AND r.sourceType = :sourceType`
- **SELECT**: `SELECT COALESCE(MAX(r.attemptNo), 0) FROM LearningResultEntity r WHERE r.userId = :userId AND r.targetType = :targetType AND r.targetId = :targetId`
- **INSERT**: `INSERT INTO learning_results (...) VALUES (...)`
- **UPSERT**: `INSERT/UPDATE INTO learning_summaries (...)`

---

## Part 4: Operational Notes

- **Idempotency**: Đảm bảo an toàn khi retry thông qua duy nhất `(sourceId, sourceType)`.
- **Tenant Isolation**: Thuộc quyền quản lý phân vùng dữ liệu theo `userId`.
- **Audit Logging**: Lưu `created_at` và `updated_at` tự động.
