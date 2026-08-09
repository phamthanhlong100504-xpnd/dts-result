# Consumer Blueprint: Learning Result Event

---

## Part 0 — Classification & Identity

- **API Name**: Consume Learning Result Event
- **API Type**: Internal (Kafka Consumer)
- **Module**: Result Service
- **Feature**: Sync Learning Result
- **Description**: Lắng nghe sự kiện `LEARNING_RESULT_CREATED` từ Kafka để lưu trữ kết quả chi tiết vào `learning_results` và cập nhật thông tin tổng hợp vào `learning_summaries`.
- **Related Tables**: `learning_results`, `learning_summaries`
- **Related Services**: Examination Service, Practice Service, Lesson Service

---

## Part 1 — API Contract

### Endpoint

- **Protocol**: Kafka
- **Topic**: `learning-results`
- **Consumer Group**: `result-service`

### Request (Event Payload)

| Name | Type | Required | Description | Validation Rules |
|------|------|----------|-------------|------------------|
| eventId | UUID | Yes | Unique identifier của event | Must be a valid UUID |
| eventType | String | Yes | Loại event | Must be `LEARNING_RESULT_CREATED` |
| eventVersion | Integer | Yes | Version của event schema | |
| occurredAt | DateTime | Yes | Thời điểm event phát sinh | ISO-8601 UTC |
| userId | UUID | Yes | ID người thực hiện | Must be a valid UUID |
| sourceType | String | Yes | Loại session gốc | Enum: `EXAM_SESSION`, `PRACTICE_SESSION`, `LESSON_SESSION`, `ASSIGNMENT_SESSION` |
| sourceId | UUID | Yes | ID session gốc | Must be a valid UUID |
| targetType | String | Yes | Loại đối tượng học | Enum: `EXAM`, `PRACTICE_SET`, `CHAPTER`, `LESSON`, `LEARNING_PROGRAM` |
| targetId | UUID | Yes | ID đối tượng học | Must be a valid UUID |
| attemptNo | Integer | Yes | Lần thực hiện (từ source) | Greater than 0 |
| result | String | Yes | Trạng thái kết quả | Enum: `PASSED`, `FAILED`, `COMPLETED`, `ABANDONED`, `EXPIRED` |
| score | Numeric | No | Điểm đạt được | Minimum 0 |
| maxScore | Numeric | No | Điểm tối đa | Greater than or equal to score |
| progress | Numeric | Yes | % hoàn thành | Between 0 and 100 |
| durationSeconds | Integer | Yes | Thời điểm thực hiện (giây) | Minimum 0 |
| startedAt | DateTime | No | Thời điểm bắt đầu | ISO-8601 UTC |
| completedAt | DateTime | Yes | Thời điểm kết thúc | ISO-8601 UTC |
| resultSnapshot | Object | No | Snapshot đặc thù của source service | JSON Object |
| metadata | Object | No | Dữ liệu mở rộng | JSON Object |

### Response
None (Fire and forget, or ACK to Kafka).

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|------------|-------------|------------------|----------------|
| DESERIALIZATION_ERROR | N/A | Cannot parse event payload | N/A |
| VALIDATION_ERROR | N/A | Event payload violates schema rules | N/A |
| DUPLICATE_EVENT | N/A | Event has already been processed | N/A |

---

## Part 2 — Processing Specification

### 1. Consumer Layer (Listener)

1. Receive event from Kafka topic `learning-results`.
2. Deserialize JSON payload to `LearningResultEvent`.
3. If deserialization fails, publish to Dead Letter Topic (DLT) and ACK.
4. Input Validation:
   - Validate required fields.
   - Validate numeric constraints.
   - Validate enums (`sourceType`, `targetType`, `result`).
   - If validation fails, publish to DLT and ACK.
5. Pass valid event to Service Layer.

### 2. Service Layer

1. **Idempotency Check**:
   - Query `learning_results` by `sourceId` and `sourceType`.
   - If a record exists, this event has already been processed. Return successfully (ACK).

2. **Transaction Boundary**: Start database transaction.

3. **Business Workflow**:
   - **Step 1: Save Learning Result**
     - Map event payload to `LearningResult` entity.
     - Tính toán `attemptNo`: Truy vấn count từ bảng `learning_results` cho `userId`, `targetId`, `targetType`. `attemptNo` mới = count + 1. (Hoặc sử dụng `attemptNo` từ event, nhưng ưu tiên tự tính dựa vào DB Result Service để đảm bảo nhất quán dữ liệu của Result).
     - Insert `LearningResult` vào DB.
   - **Step 2: Update Learning Summary**
     - Query `learning_summaries` by `userId`, `targetType`, `targetId` (sử dụng pessimistic lock `FOR UPDATE` để tránh race condition).
     - Nếu chưa tồn tại:
       - Tạo mới `LearningSummary`.
       - `attempt_count` = 1
       - `completion_count` = 1 (nếu result là PASSED hoặc COMPLETED), ngược lại 0.
       - `best_score` = score
       - `latest_score` = score
       - `average_score` = score
       - `progress` = progress
       - `total_duration_seconds` = durationSeconds
       - `status` = Tính toán từ result (ví dụ: `COMPLETED` nếu PASSED/COMPLETED, ngược lại `IN_PROGRESS`).
       - `last_activity_at` = completedAt
       - `completed_at` = completedAt (nếu PASSED/COMPLETED).
     - Nếu đã tồn tại:
       - Cập nhật `LearningSummary` hiện tại.
       - `attempt_count` = `attempt_count` + 1
       - `completion_count` = `completion_count` + 1 (nếu result là PASSED hoặc COMPLETED).
       - `latest_score` = score
       - `best_score` = MAX(`best_score`, score)
       - `total_duration_seconds` = `total_duration_seconds` + durationSeconds
       - `last_activity_at` = MAX(`last_activity_at`, completedAt)
       - `progress` = MAX(`progress`, progress)
       - Tự tính `average_score` = `(average_score * (attempt_count - 1) + score) / attempt_count`.
       - Cập nhật `status` = `COMPLETED` (nếu trước đó là `IN_PROGRESS` và result hiện tại là PASSED/COMPLETED).
       - Cập nhật `completed_at` = completedAt (nếu chưa có và result là PASSED/COMPLETED).
     - Save (Insert/Update) `LearningSummary` vào DB.

4. **Commit Transaction**.

### 3. Repository Layer

1. Database Operations:
   - Check existence in `learning_results`.
   - Insert into `learning_results`.
   - Query `learning_summaries`.
   - Insert/Update `learning_summaries`.
2. Expected Queries:
   - `SELECT EXISTS (SELECT 1 FROM learning_results WHERE source_id = ? AND source_type = ?)`
   - `SELECT COUNT(*) FROM learning_results WHERE user_id = ? AND target_id = ? AND target_type = ?`
   - `INSERT INTO learning_results (...) VALUES (...)`
   - `SELECT * FROM learning_summaries WHERE user_id = ? AND target_id = ? AND target_type = ? FOR UPDATE`
   - `INSERT INTO learning_summaries (...) VALUES (...)`
   - `UPDATE learning_summaries SET ... WHERE id = ?`
3. Persistence Operations:
   - Managed by Spring Data JPA.

### 4. External Interaction

- None (Consumer only receives from Kafka).

### 5. Validation

- **Request Validation**: Schema validation (`LearningResultEventValidator`).
- **Business Validation**: Idempotency check.
- **Permission Validation**: Not applicable for background events.

---

## Part 3 — Data Interaction

- **Operation Type**: SELECT
- **Target Table**: `learning_results`
- **Conditions**: `source_id = ? AND source_type = ?`
- **Expected Result**: Boolean (Exists/Not Exists) for Idempotency.

- **Operation Type**: SELECT
- **Target Table**: `learning_results`
- **Conditions**: `user_id = ? AND target_id = ? AND target_type = ?`
- **Expected Result**: Aggregate Count.

- **Operation Type**: INSERT
- **Target Table**: `learning_results`
- **Conditions**: None
- **Expected Result**: 1 row inserted.

- **Operation Type**: SELECT
- **Target Table**: `learning_summaries`
- **Conditions**: `user_id = ? AND target_type = ? AND target_id = ?` (FOR UPDATE)
- **Expected Result**: 1 or 0 row.

- **Operation Type**: INSERT / UPDATE
- **Target Table**: `learning_summaries`
- **Conditions**: `id = ?` (for update)
- **Expected Result**: 1 row inserted or updated.

---

## Part 4 — Operational Notes

- **Idempotency**: Implemented via `uq_learning_results_source` (Unique Index on `source_id`, `source_type`) AND programmatic check before processing.
- **Tenant Isolation**: Not Specified.
- **Retry Strategy**: 
  - Transient errors (DB connection, timeout): Retry with Exponential Backoff (1s, 2s, 5s).
  - Permanent errors (Validation, Deserialization): Publish to Dead Letter Topic (`learning-results-dlt`).
- **Audit Logging**: Log consumer start, idempotency skips, and successful processing.
- **Monitoring**: Track `events.received`, `events.processed`, `events.failed`, `events.dlt`.
- **Metrics**: Measure `consume.duration`.
- **Tracing**: Extract trace ID from Kafka headers to continue tracing from source service.
