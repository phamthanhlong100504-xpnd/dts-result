# API Blueprint — dts-result (Result & Tracking Service)

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

# 1. Submit Content Attempt API

## Part 0 — Classification & Identity

- **API Name**: Submit Content Attempt API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Submission & Evaluation Processing
- **Description**: Receives student attempt submission for assignments, quizzes, exams, or interactive question items. Evaluates objective answers (auto-grading), calculates final score and pass status, persists attempt audit record, and updates the learner's overall content progress tree.
- **Related Tables**: `user_content_attempts`, `user_content_results`
- **Related Services**: `lms-content-builder` (Content metadata reference), `lms-exam` (Exam session reference)

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
| `durationSec` | Integer | Yes | Tổng thời gian mở phiên làm (giây) | Integer >= 0 |
| `timeTakenSec` | Integer | Yes | Thời gian làm thực tế (giây) | Integer >= 0 |
| `deviceKind` | String | No | Loại thiết bị | Enum: `desktop`, `mobile`, `tablet` |
| `sessionId` | String | No | Session ID phiên làm bài trên ứng dụng | Format UUID v4 |
| `contentVersionId` | String | No | Version bài thi / câu hỏi lúc làm | Format UUID v4 |
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

1. Decode HTTP headers `X-Tenant-Id` and `X-User-Id`. Return `RES-401-001` if missing.
2. Validate incoming JSON request body schema against constraints (check required fields, enum values, timestamp formats). Return `RES-400-001` or `RES-400-002` on validation failure.
3. Map validated request DTO into application domain command object.
4. Delegate command execution to `SubmitAttemptService`.
5. Map returned attempt domain model into response DTO and return HTTP status `201 Created`.

---

### Service Layer

1. Verify tenant access authorization for the learner (`userId`).
2. Query existing attempts count for `(tenantId, userId, contentId)` to derive the next sequence number `seqNo = max(seq_no) + 1`.
3. Process automatic answer grading for objective question types (SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE):
   - Calculate `score` based on correct answer keys.
   - Set `gradingStatus = graded` if all questions are auto-gradable; otherwise set `gradingStatus = pending`.
4. Calculate penalty points `penaltyScore` (if marked late or hints used).
5. Calculate `finalScore = max(0, score - penaltyScore)`.
6. Determine `isPassed` by comparing `finalScore` with pass threshold specified for `contentId`.
7. Persist attempt record into repository.
8. If `nodeId` is specified, trigger progress rollup on `user_content_results`:
   - Increment `attempt_count`.
   - Update `last_score = finalScore`, `last_score_at = submittedAt`, `last_attempt_id = attemptId`.
   - Recalculate `best_score = max(best_score, finalScore)`.
   - Update node `status = COMPLETED` and `percent = 100` if `isPassed` is true.
9. Emit outbox event `content_attempt.submitted` for downstream CDC / message broker integration.

---

### Repository Layer

1. Query database for current max `seq_no` for `(tenant_id, user_id, content_id)`.
2. Insert new row into `user_content_attempts`.
3. Query `user_content_results` by `(tenant_id, user_id, id)`.
4. Update `user_content_results` record with new rollup totals, last score, best score, and completion timestamp.

---

### External Interaction

- **Kafka**: Emit event to topic `lms.result.attempt-submitted` containing attempt ID, user ID, content ID, final score, and completion status.
- **Redis**: Invalidate cached user attempt summary keys matching `cache:attempts:{tenantId}:{userId}:{contentId}`.

---

### Validation

#### Request Validation

- `X-Tenant-Id` and `X-User-Id` must be valid UUIDs.
- `contentId` must be a valid UUID.
- `contentType` must match enum (`ASSIGNMENT`, `EXAM`, `QUIZ`, `QUESTION`).
- `startedAt` <= `submittedAt`.
- `durationSec` >= 0 and `timeTakenSec` >= 0.

#### Business Validation

- Attempt sequence `seqNo` must be strictly monotonic for `(tenantId, userId, contentId)`.
- If `gradingStatus` is `graded`, `finalScore` must not exceed `maxScore`.

#### Permission Validation

- Caller must possess valid tenant membership matching `X-Tenant-Id`.
- Learner can only submit attempt for own `userId`.

---

## Part 3 — Data Interaction

### Operation 1: Query Max Attempt Sequence Number

- **Operation Type**: `SELECT`
- **Target Table**: `user_content_attempts`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND content_id = :contentId`
- **Expected Result**: Maximum integer `seq_no` value, or `0` if no previous attempt exists.
- **Performance Notes**: Supported by compound index `ix_uca_user_content (tenant_id, user_id, content_id, started_at)`.

---

### Operation 2: Insert Attempt Record

- **Operation Type**: `INSERT`
- **Target Table**: `user_content_attempts`
- **Conditions**: Primary key `id` generated via UUID v7; unique constraint on `(tenant_id, user_id, content_id, seq_no)`.
- **Expected Result**: Single row inserted representing the submission attempt.

---

### Operation 3: Update User Content Progress Node

- **Operation Type**: `UPDATE`
- **Target Table**: `user_content_results`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND id = :nodeId`
- **Expected Result**: Single row updated with `attempt_count = attempt_count + 1`, `last_score = :finalScore`, `best_score = GREATEST(COALESCE(best_score, 0), :finalScore)`, `last_score_at = :submittedAt`, `last_attempt_id = :attemptId`, and `updated_at = NOW()`.
- **Performance Notes**: Supported by primary key lookup `pk_user_content_results (id)`.

---

## Part 4 — Operational Notes

- **Idempotency**: Guaranteed by unique constraint `uq_uca_user_content_seq` on `(tenant_id, user_id, content_id, seq_no)`.
- **Tenant Isolation**: Mandatory `tenant_id` filtering applied across every repository operation.
- **Retry Strategy**: Synchronous endpoint with retry safe behavior when client includes explicit idempotency or retry headers.
- **Audit Logging**: Write audit log entry capturing attempt ID, user ID, content ID, score, and timestamps.
- **Monitoring**: Track submission error rate, auto-grading execution latency, and score distribution metrics.
- **Metrics**: `result_service_attempts_submitted_total`, `result_service_attempt_grading_latency_seconds`.
- **Tracing**: Propagate W3C `traceparent` context header into database transaction contexts and Kafka event headers.

---
---

# 2. Get Learner Content Progress Tree API

## Part 0 — Classification & Identity

- **API Name**: Get Learner Content Progress Tree API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Progress Tree Query
- **Description**: Fetches hierarchical or list progress records (completion status, completion percentage, total learning time, attempt rollups, best scores) for a given learner across learning tree nodes.
- **Related Tables**: `user_content_results`
- **Related Services**: `lms-content-builder`

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
| `userId` | String | Yes | ID learner cần lấy tiến độ | Format UUID v4 |

#### Query Parameters

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `contentCode` | String | No | Materialized path prefix để lọc nhánh cây | Text pattern (ví dụ: `UUID1:UUID2%`) |
| `status` | String | No | Lọc theo trạng thái hoàn thành | Enum: `NOT_COMPLETED`, `COMPLETED` |
| `page` | Integer | No | Số trang pagination | Integer >= 1, Mặc định: 1 |
| `size` | Integer | No | Kích thước trang | Integer 1–100, Mặc định: 50 |

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
| `items[].tenantId` | String | ID tenant |
| `items[].userId` | String | ID learner |
| `items[].contentId` | String | ID đối tượng nội dung |
| `items[].contentType` | String | Bảng/loại nội dung (`videos`, `lessons`, `coursewares`) |
| `items[].contentVersionId` | String | Version nội dung đang học (nếu có) |
| `items[].parentNodeId` | String | ID node cha trực tiếp (NULL nếu là gốc) |
| `items[].contentCode` | String | Materialized path (ví dụ: `UUID1:UUID2:UUID3`) |
| `items[].status` | String | Trạng thái (`NOT_COMPLETED`, `COMPLETED`) |
| `items[].percent` | Integer | Phần trăm hoàn thành (0–100) |
| `items[].totalLearnSec` | Integer | Tổng thời gian học (giây) |
| `items[].learnCount` | Integer | Số phiên xem/đọc nội dung |
| `items[].attemptCount` | Integer | Số lần nộp bài tập |
| `items[].bestScore` | Decimal | Điểm cao nhất đạt được |
| `items[].lastScore` | Decimal | Điểm lần nộp gần nhất |
| `items[].lastScoreAt` | String | Thời điểm nộp/chấm gần nhất |
| `items[].startedAt` | String | Thời điểm bắt đầu học lần đầu |
| `items[].lastActivityAt` | String | Thời điểm hoạt động gần nhất |
| `items[].completedAt` | String | Thời điểm hoàn thành node |
| `page` | Integer | Số trang hiện tại |
| `size` | Integer | Số lượng bản ghi mỗi trang |
| `total` | Long | Tổng số bản ghi thỏa điều kiện |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Parameter query không hợp lệ | Invalid query parameters or pagination range. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. |
| `RES-403-001` | 403 | Không có quyền xem tiến độ user | Access denied to learner progress records. |
| `RES-404-001` | 404 | Không tìm thấy dữ liệu tiến độ | No progress records found for specified user and content criteria. |
| `RES-500-001` | 500 | Lỗi truy vấn cơ sở dữ liệu | Error occurred while retrieving progress records. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Decode header `X-Tenant-Id` and path parameter `userId`.
2. Parse and validate query parameters (`contentCode`, `status`, `page`, `size`).
3. Delegate to `GetProgressService`.
4. Wrap returned progress model into paginated response structure and return HTTP `200 OK`.

---

### Service Layer

1. Validate authorization: learner accessing own progress or administrative role with tenant read access.
2. Query repository for matching progress node rows.
3. Compute total matching count for pagination metadata.
4. Return assembled list of node progress items.

---

### Repository Layer

1. Execute filtered read query on `user_content_results` table matching `tenant_id`, `user_id`, optional `content_code` prefix, and optional `status`.
2. Apply ordering `last_activity_at DESC` and pagination offset/limit.

---

### External Interaction

None.

---

### Validation

#### Request Validation

- `X-Tenant-Id` and `userId` must be valid UUIDs.
- `page` >= 1 and `size` between 1 and 100.
- `status` must be `NOT_COMPLETED` or `COMPLETED` if supplied.

#### Business Validation

- Read operation must strictly scope results to `tenant_id` and `user_id`.

#### Permission Validation

- Caller user ID must equal `userId` or possess `ROLE_INSTRUCTOR` / `ROLE_ADMIN` within the tenant.

---

## Part 3 — Data Interaction

### Operation 1: Query Learner Content Results Tree

- **Operation Type**: `SELECT`
- **Target Table**: `user_content_results`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND (:contentCode IS NULL OR content_code LIKE :contentCodePrefix) AND (:status IS NULL OR status = :status)`
- **Expected Result**: List of progress node records sorted by `last_activity_at DESC` with limit `:size` and offset `(:page - 1) * :size`.
- **Performance Notes**: Supported by indexes `ix_ucr_user_status_activity (tenant_id, user_id, status, last_activity_at)` and `ix_ucr_content_code (tenant_id, user_id, content_code text_pattern_ops)`.

---

## Part 4 — Operational Notes

- **Idempotency**: Read-only query endpoint; naturally idempotent.
- **Tenant Isolation**: Mandatory `tenant_id` column predicate on every SQL query.
- **Retry Strategy**: Safe for automatic client retries upon transient network failure.
- **Audit Logging**: Not required for read endpoints.
- **Monitoring**: Track 95th/99th percentile query latency for tree progress fetching.
- **Metrics**: `result_service_progress_query_seconds`, `result_service_progress_query_total`.
- **Tracing**: Propagate trace contexts across API layer and database connection pools.

---
---

# 3. Record Learning Event API

## Part 0 — Classification & Identity

- **API Name**: Record Learning Event API
- **API Type**: Public / Internal
- **Module**: Tracking & Telemetry
- **Feature**: Append-Only Event Recording
- **Description**: Ingests append-only learning lifecycle events (`lesson.started`, `lesson.completed`, `block.entered`, `block.left`, `block.completed`) from client applications or backend services for processing, CDC fan-out, and ClickHouse analytics synchronization.
- **Related Tables**: `tracking_events`
- **Related Services**: `lms-delivery`, `lms-assignment`, `ClickHouse-Sink`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `POST`
- **URL**: `/api/v1/result-service/tracking-events`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant phát sinh event | Format UUID v4 |
| `X-User-Id` | String | Yes | ID learner thực hiện hành động | Format UUID v4 |

#### Path Variables

None.

#### Query Parameters

None.

#### Request Body

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `eventType` | String | Yes | Loại sự kiện vòng đời | Enum: `lesson.started`, `lesson.completed`, `block.entered`, `block.left`, `block.completed` |
| `entityKind` | String | Yes | Loại đối tượng gốc | Enum: `lesson`, `courseware` |
| `entityId` | String | Yes | ID đối tượng gốc | Format UUID v4 |
| `versionId` | String | Yes | ID version cụ thể đang học | Format UUID v4 |
| `versionNo` | Integer | Yes | Số version | Integer >= 1 |
| `language` | String | Yes | Mã ngôn ngữ của version | Non-empty string (ví dụ: `vi-VN`, `en-US`) |
| `blockId` | String | Conditional | ID block trong snapshot (Bắt buộc với `block.*`) | Format UUID v4 nếu có |
| `nodePath` | String | Conditional | Path từ root tới block (Bắt buộc với `block.*`) | Non-empty string nếu có |
| `occurredAt` | String | Yes | Thời điểm sự kiện xảy ra tại client/service | Format ISO-8601 UTC, `occurredAt` <= NOW |
| `source` | String | Yes | Nguồn phát sự kiện | Enum: `client_web`, `client_mobile`, `service:delivery`, `service:assignment`, `service:discussion`, `service:access` |
| `idempotencyKey` | String | Yes | Khóa chống ghi trùng | Non-empty unique string (Max length 128) |
| `payload` | Object | Yes | Dữ liệu động theo eventType | JSON Object |
| `context` | Object | No | Metadata môi trường client (IP, device, session) | JSON Object |

---

### Response

- **Success Status**: `201 Created`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `id` | String | ID sự kiện được tạo (`tracking_events.id`) |
| `tenantId` | String | ID tenant |
| `userId` | String | ID learner |
| `eventType` | String | Loại sự kiện đã ghi nhận |
| `receivedAt` | String | Thời điểm hệ thống nhận sự kiện |
| `idempotencyKey` | String | Khóa idempotency đã xử lý |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Dữ liệu sự kiện không hợp lệ hoặc thiếu `blockId`/`nodePath` | Event parameters invalid or missing block parameters for block level event. |
| `RES-400-002` | 400 | Thời điểm xảy ra sự kiện ở tương lai | Timestamp anomaly: occurredAt cannot be in the future. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. |
| `RES-403-001` | 403 | Không có quyền ghi nhận sự kiện tenant | Access denied for specified tenant event ingestion. |
| `RES-409-001` | 409 | Khóa idempotency đã tồn tại (Event đã được ghi nhận trước đó) | Idempotent event duplicate detected. Event already recorded. |
| `RES-500-001` | 500 | Lỗi lưu trữ sự kiện hệ thống | Unexpected internal error while recording tracking event. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Read HTTP headers `X-Tenant-Id` and `X-User-Id`.
2. Validate request body parameters against rules (check enums, ensure `blockId` and `nodePath` are present for `block.*` event types).
3. Delegate event recording command to `RecordTrackingEventService`.
4. Return `201 Created` with event metadata.

---

### Service Layer

1. Check for duplicate event using `idempotencyKey` and `tenantId`. If duplicate exists, return existing event details with status `201 Created` (idempotent success).
2. Construct immutable `tracking_events` entity with `received_at = NOW()`.
3. Save entity to repository.
4. Emit outbox message to Kafka topic `lms.tracking.events` for asynchronous ingestion by ClickHouse sink and background progress calculation workers.

---

### Repository Layer

1. Perform single row `INSERT` into `tracking_events` table.

---

### External Interaction

- **Kafka**: Publish event payload to topic `lms.tracking.events` for real-time analytics streaming.

---

### Validation

#### Request Validation

- `X-Tenant-Id`, `X-User-Id`, `entityId`, `versionId` must be valid UUIDs.
- If `eventType` starts with `block.`, both `blockId` and `nodePath` must be non-null.
- `occurredAt` <= system current timestamp.
- `idempotencyKey` must not be blank.

#### Business Validation

- Idempotency key `idempotencyKey` must be unique per tenant.

#### Permission Validation

- Requester identity must match `X-User-Id` or hold valid service-to-service credentials.

---

## Part 3 — Data Interaction

### Operation 1: Insert Append-Only Tracking Event

- **Operation Type**: `INSERT`
- **Target Table**: `tracking_events`
- **Conditions**: Primary key `id` generated via UUID v4; unique constraint on `(tenant_id, idempotency_key)`.
- **Expected Result**: Single row inserted into append-only event store.

---

## Part 4 — Operational Notes

- **Idempotency**: Guaranteed by unique constraint `uq_tracking_events_tenant_idempotency` on `(tenant_id, idempotency_key)`.
- **Tenant Isolation**: Mandatory `tenant_id` scope on every database constraint and query.
- **Retry Strategy**: Safe for client/service retries thanks to strict idempotency deduplication.
- **Audit Logging**: Event store is append-only and immutable, serving as audit history.
- **Monitoring**: Track event ingestion rate (events/sec), duplicate event rate, and Kafka delivery delay.
- **Metrics**: `tracking_events_recorded_total`, `tracking_events_duplicate_total`.
- **Tracing**: Propagate trace identifiers into event `context` JSON object and Kafka message headers.
