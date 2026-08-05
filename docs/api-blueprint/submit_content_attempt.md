# API Blueprint — Nộp bài làm & chấm điểm

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Nộp bài làm & chấm điểm API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Submission & Evaluation Processing
- **Description**: Tiếp nhận bài làm / bài thi / quiz của học viên. Tự động chấm điểm các câu hỏi trắc nghiệm/khách quan, tính điểm trừ (penalty) và điểm cuối cùng, lưu bản ghi lần nộp (`user_content_attempts`), cập nhật rollup tiến độ học tập (`user_content_results`) và phát sự kiện Outbox cho hệ thống thông báo/thống kê.
- **Related Tables**: `user_content_attempts`, `user_content_results`
- **Related Services**: `lms-content-builder` (Truy xuất đáp án & metadata câu hỏi), `lms-exam` (Tham chiếu phiên thi)

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `POST`
- **URL**: `/api/v1/result-service/content-attempts`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant sở hữu | Format UUID v4 |
| `X-User-Id` | String | Yes | ID learner thực hiện nộp bài | Format UUID v4 |

#### Path Variables

None.

#### Query Parameters

None.

#### Request Body

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `nodeId` | String | No | ID vị trí node trong cây tiến độ (`user_content_results.id`) | Format UUID v4 nếu có |
| `contentId` | String | Yes | ID đối tượng bài tập / bài thi / quiz | Format UUID v4 |
| `contentType` | String | Yes | Loại đối tượng nội dung | Enum: `ASSIGNMENT`, `EXAM`, `QUIZ`, `QUESTION` |
| `startedAt` | String | Yes | Thời điểm bắt đầu làm bài | Format ISO-8601 UTC |
| `submittedAt` | String | Yes | Thời điểm người dùng bấm nộp bài | Format ISO-8601 UTC, `submittedAt` >= `startedAt` |
| `durationSec` | Integer | Yes | Tổng thời gian mở phiên làm bài (giây) | Integer >= 0 |
| `timeTakenSec` | Integer | Yes | Thời gian làm thực tế (giây) | Integer >= 0 |
| `deviceKind` | String | No | Loại thiết bị | Enum: `desktop`, `mobile`, `tablet` |
| `sessionId` | String | No | Session ID phiên làm bài trên ứng dụng | Format UUID v4 |
| `contentVersionId` | String | No | Snapshot version bài thi / câu hỏi lúc làm | Format UUID v4 |
| `sourceService` | String | No | Tên dịch vụ gửi yêu cầu | Max length 100 |
| `sourceRef` | String | No | ID tham chiếu bên dịch vụ nguồn | Max length 100 |
| `answers` | Array of Objects | No | Mảng chứa danh sách câu trả lời | Mảng JSON object |
| `answers[].questionId` | String | Yes | ID câu hỏi | Format UUID v4 |
| `answers[].questionVersionId` | String | Yes | Version của câu hỏi lúc trả lời | Format UUID v4 |
| `answers[].answer` | Object | Yes | Nội dung câu trả lời polymorph theo loại câu hỏi | JSON Object không rỗng |
| `proctoringData` | Object | No | Dữ liệu giám sát thi (nếu có) | JSON Object |

---

### Response

- **Success Status**: `201 Created`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `id` | String | ID bản ghi lần nộp bài (`user_content_attempts.id`) |
| `tenantId` | String | ID tenant sở hữu |
| `userId` | String | ID learner |
| `contentId` | String | ID nội dung bài thi/tập |
| `contentType` | String | Loại đối tượng bài |
| `seqNo` | Integer | Số thứ tự lần nộp bài (1-based sequence number) |
| `startedAt` | String | Thời điểm bắt đầu |
| `submittedAt` | String | Thời điểm nộp bài |
| `status` | String | Trạng thái nộp (`submitted`, `auto_submitted`) |
| `gradingStatus` | String | Trạng thái chấm (`graded`, `pending`) |
| `score` | Decimal | Điểm thô đạt được (NULL nếu chờ chấm tay) |
| `maxScore` | Decimal | Điểm tối đa |
| `penaltyScore` | Decimal | Điểm trừ (nộp trễ, gợi ý) |
| `finalScore` | Decimal | Điểm cuối cùng = score - penaltyScore |
| `isPassed` | Boolean | Kết quả Đạt / Không đạt |
| `timeTakenSec` | Integer | Thời gian làm thực tế (giây) |
| `createdAt` | String | Thời điểm tạo bản ghi |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Dữ liệu request không hợp lệ | Malformed request parameters or invalid payload body. |
| `RES-400-002` | 400 | Thời gian nộp nhỏ hơn thời gian bắt đầu | Submitted timestamp cannot be earlier than started timestamp. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid user identity headers. |
| `RES-403-001` | 403 | Không có quyền truy cập tenant | Access denied for the specified tenant. |
| `RES-404-001` | 404 | Không tìm thấy bài tập hoặc node tiến độ | Targeted content or progress node not found. |
| `RES-409-001` | 409 | Xung đột thứ tự lần nộp (sequence conflict) | Attempt sequence conflict. Retried with duplicated sequence number. |
| `RES-422-001` | 422 | Cấu trúc câu trả lời không đúng quy định | Invalid format for submitted question answers. |
| `RES-500-001` | 500 | Lỗi xử lý chấm điểm hoặc lưu trữ hệ thống | An unexpected error occurred while processing the attempt submission. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Đọc và kiểm tra các HTTP Header bắt buộc `X-Tenant-Id` và `X-User-Id`. Trả về lỗi `RES-401-001` nếu thiếu.
2. Kiểm tra tính hợp lệ của Request Body (cấu trúc JSON, thuộc tính bắt buộc, định dạng UUID, Enum, mốc thời gian). Trả về `RES-400-001` hoặc `RES-400-002` nếu không thỏa mãn.
3. Chuyển đổi dữ liệu Request DTO thành Domain Command object.
4. Gọi Service layer để xử lý nghiệp vụ nộp bài và chấm điểm.
5. Chuyển đổi kết quả thu được từ Domain Model sang Response DTO và trả về phản hồi HTTP `201 Created`.

---

### Service Layer

1. Xác thực quyền hạn làm bài của learner (`userId`) trong phạm vi tenant.
2. Truy vấn số lần nộp bài hiện tại của `(tenantId, userId, contentId)` để tính số thứ tự lần nộp tiếp theo `seqNo = max(seq_no) + 1`.
3. Đánh giá và chấm điểm tự động đối với các loại câu hỏi trắc nghiệm (SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE):
   - So sánh câu trả lời của learner với đáp án chuẩn.
   - Tính toán điểm thô `score` đạt được và tổng điểm `maxScore`.
   - Đặt `gradingStatus = graded` nếu toàn bộ câu hỏi được chấm tự động, ngược lại đặt `gradingStatus = pending` (chờ giáo viên chấm câu hỏi tự luận/tự luận ngắn).
4. Tính toán điểm trừ `penaltyScore` (nếu có cấu hình trừ điểm do nộp trễ hạn hoặc sử dụng gợi ý).
5. Tính điểm cuối cùng: `finalScore = max(0, score - penaltyScore)`.
6. Xử lý trạng thái Đạt / Không đạt: So sánh `finalScore` với ngưỡng điểm đạt (`pass_threshold`) của bài tập để đặt cờ `isPassed`.
7. Ghi bản ghi nộp bài mới vào Repository.
8. Nếu request có chứa `nodeId`, thực hiện cập nhật rollup tiến độ học tập trên bảng `user_content_results`:
   - Tăng `attempt_count` thêm 1.
   - Cập nhật `last_score = finalScore`, `last_score_at = submittedAt`, `last_attempt_id = attemptId`.
   - Tính toán lại điểm cao nhất: `best_score = max(best_score, finalScore)`.
   - Cập nhật `status = COMPLETED` và `percent = 100` nếu bài làm đạt tiêu chuẩn hoàn thành.
9. Đăng ký sự kiện Outbox `content_attempt.submitted` để hạ tầng CDC đồng bộ sang Kafka.

---

### Repository Layer

1. Thực thi truy vấn tìm `seq_no` lớn nhất cho bộ khóa `(tenant_id, user_id, content_id)`.
2. Thực thi chèn bản ghi nộp bài mới vào bảng `user_content_attempts`.
3. Truy vấn bản ghi tiến độ `user_content_results` theo `(tenant_id, user_id, id)`.
4. Thực thi cập nhật chỉ số rollup, điểm số gần nhất, điểm số cao nhất và thời điểm nộp cho node tiến độ.

---

### External Interaction

- **Kafka**: Đẩy sự kiện tới topic `lms.result.attempt-submitted` chứa thông tin lần nộp (attempt ID, user ID, content ID, điểm số, trạng thái đạt).
- **Redis**: Xóa bộ nhớ đệm thông tin tổng hợp lần nộp của học viên theo key format `cache:attempts:{tenantId}:{userId}:{contentId}`.

---

### Validation

#### Request Validation

- `X-Tenant-Id` và `X-User-Id` phải là chuỗi UUID v4 hợp lệ.
- `contentId` phải là UUID v4 hợp lệ.
- `contentType` thuộc danh sách Enum (`ASSIGNMENT`, `EXAM`, `QUIZ`, `QUESTION`).
- `startedAt` <= `submittedAt`.
- `durationSec` >= 0 và `timeTakenSec` >= 0.

#### Business Validation

- Số thứ tự lần nộp `seqNo` phải tăng liên tục cho cùng một `(tenantId, userId, contentId)`.
- Nếu trạng thái là `graded`, `finalScore` không được vượt quá `maxScore`.

#### Permission Validation

- Người dùng gửi yêu cầu phải thuộc tenant chỉ định.
- Học viên chỉ được phép nộp bài cho chính `userId` của mình.

---

## Part 3 — Data Interaction

### Operation 1: Lấy số thứ tự lượt nộp tiếp theo

- **Operation Type**: `SELECT`
- **Target Table**: `user_content_attempts`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND content_id = :contentId`
- **Expected Result**: Trả về giá trị `seq_no` lớn nhất hiện tại (hoặc 0 nếu chưa có lần nộp nào).
- **Performance Notes**: Tận dụng chỉ mục compound `ix_uca_user_content (tenant_id, user_id, content_id, started_at)`.

---

### Operation 2: Chèn bản ghi nộp bài mới

- **Operation Type**: `INSERT`
- **Target Table**: `user_content_attempts`
- **Conditions**: Khóa chính `id` tự sinh bằng UUID v7; ràng buộc duy nhất trên `(tenant_id, user_id, content_id, seq_no)`.
- **Expected Result**: Chèn thành công 1 bản ghi nộp bài mới.

---

### Operation 3: Cập nhật chỉ số tiến độ học tập của Node

- **Operation Type**: `UPDATE`
- **Target Table**: `user_content_results`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND id = :nodeId`
- **Expected Result**: Cập nhật 1 bản ghi với `attempt_count = attempt_count + 1`, `last_score = :finalScore`, `best_score = GREATEST(COALESCE(best_score, 0), :finalScore)`, `last_score_at = :submittedAt`, `last_attempt_id = :attemptId` và `updated_at = NOW()`.
- **Performance Notes**: Tận dụng tìm kiếm theo khóa chính `pk_user_content_results (id)`.

---

## Part 4 — Operational Notes

- **Idempotency**: Đảm bảo tính chống ghi trùng bằng ràng buộc duy nhất `uq_uca_user_content_seq` trên `(tenant_id, user_id, content_id, seq_no)`.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi thao tác truy vấn và lưu trữ dữ liệu.
- **Retry Strategy**: Yêu cầu đồng bộ, client có thể thử lại an toàn khi gặp sự cố mạng nếu kèm theo khóa idempotency.
- **Audit Logging**: Bản ghi `user_content_attempts` lưu trữ lịch sử bất biến sau khi nộp, đóng vai trò nhật ký vết pháp lý cho mỗi lần làm bài.
- **Monitoring**: Theo dõi tỷ lệ nộp bài lỗi, thời gian chấm điểm tự động và phân bố điểm số.
- **Metrics**: `result_service_attempts_submitted_total`, `result_service_attempt_grading_latency_seconds`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) qua controller, database transaction và message header Kafka.
