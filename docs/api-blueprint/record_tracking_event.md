# API Blueprint — Ghi nhận sự kiện tracking

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Ghi nhận sự kiện tracking API
- **API Type**: Public / Internal
- **Module**: Tracking & Telemetry
- **Feature**: Append-Only Event Ingestion
- **Description**: Tiếp nhận và lưu trữ bất biến (append-only) các sự kiện vòng đời học tập (`lesson.started`, `lesson.completed`, `block.entered`, `block.left`, `block.completed`) từ ứng dụng Client (Web/Mobile) hoặc các dịch vụ Backend. Đảm bảo chống ghi trùng bằng khóa `idempotencyKey` và phát sự kiện Outbox cho hạ tầng ClickHouse sink.
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
| `X-Tenant-Id` | String | Yes | ID tenant phát sinh sự kiện | Format UUID v4 |
| `X-User-Id` | String | Yes | ID học viên thực hiện hành động | Format UUID v4 |

#### Path Variables

None.

#### Query Parameters

None.

#### Request Body

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `eventType` | String | Yes | Loại sự kiện vòng đời học tập | Enum: `lesson.started`, `lesson.completed`, `block.entered`, `block.left`, `block.completed` |
| `entityKind` | String | Yes | Loại entity gốc | Enum: `lesson`, `courseware` |
| `entityId` | String | Yes | ID entity logical | Format UUID v4 |
| `versionId` | String | Yes | ID bản publish cụ thể user đang học | Format UUID v4 |
| `versionNo` | Integer | Yes | Số phiên bản của nội dung | Integer >= 1 |
| `language` | String | Yes | Ngôn ngữ của bản publish | Max length 20 (ví dụ: `vi-VN`, `en-US`) |
| `blockId` | String | Conditional | ID block trong snapshot (Bắt buộc với event `block.*`) | Format UUID v4 nếu có |
| `nodePath` | String | Conditional | Path từ root tới block (Bắt buộc với event `block.*`) | Non-empty string nếu có |
| `occurredAt` | String | Yes | Thời điểm sự kiện xảy ra tại thực tế | Format ISO-8601 UTC, `occurredAt` <= `receivedAt` |
| `source` | String | Yes | Nguồn phát sự kiện | Enum: `client_web`, `client_mobile`, `service:delivery`, `service:assignment`, `service:discussion`, `service:access` |
| `idempotencyKey` | String | Yes | Khóa chống ghi trùng lặp | Max length 128, Unique per tenant |
| `payload` | Object | Yes | Dữ liệu động theo loại `eventType` | JSON Object không rỗng |
| `context` | Object | No | Metadata môi trường client (IP, device, session) | JSON Object |

---

### Response

- **Success Status**: `201 Created`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `id` | String | ID sự kiện tracking vừa được tạo (`tracking_events.id`) |
| `tenantId` | String | ID tenant |
| `userId` | String | ID học viên |
| `eventType` | String | Loại sự kiện đã ghi nhận |
| `receivedAt` | String | Thời điểm hệ thống nhận được sự kiện (ISO-8601 UTC) |
| `idempotencyKey` | String | Khóa idempotency đã xử lý |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Dữ liệu sự kiện không hợp lệ hoặc thiếu `blockId`/`nodePath` cho event block | Invalid tracking event parameters or missing mandatory block parameters. |
| `RES-400-002` | 400 | Mốc thời gian xảy ra sự kiện nằm ở tương lai | Timestamp anomaly: occurredAt cannot be in the future. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền ghi nhận sự kiện | Access denied for tracking event ingestion. |
| `RES-409-001` | 409 | Khóa idempotency đã tồn tại (Sự kiện trùng lặp) | Event idempotency key duplicate detected. Event already processed. |
| `RES-500-001` | 500 | Lỗi hệ thống khi lưu trữ sự kiện | An unexpected error occurred while persisting tracking event. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Đọc và kiểm tra HTTP Headers `X-Tenant-Id` và `X-User-Id`. Trả về `RES-401-001` nếu thiếu.
2. Kiểm tra tính hợp lệ của Request Body (kiểm tra Enum, mốc thời gian `occurredAt` <= NOW, đảm bảo `blockId` và `nodePath` phải có nếu `eventType` là `block.*`). Trả về `RES-400-001` hoặc `RES-400-002` nếu vi phạm.
3. Delegate gọi Service layer `RecordTrackingEventService`.
4. Trả về thông tin sự kiện đã lưu kèm HTTP status `201 Created`.

---

### Service Layer

1. Kiểm tra khóa `idempotencyKey` trong phạm vi `tenantId`.
   - Nếu khóa đã tồn tại trong DB, trả về bản ghi sự kiện đã ghi nhận trước đó kèm status `201 Created` (xử lý idempotent thành công).
2. Tạo đối tượng `tracking_events` mới với `received_at = NOW()`.
3. Ghi bản ghi bất biến vào Repository.
4. Phát sự kiện tới Outbox/Kafka topic `lms.tracking.events` để worker tính toán tiến độ và ClickHouse sink đồng bộ dữ liệu báo cáo.

---

### Repository Layer

1. Thực thi chèn 1 bản ghi mới vào bảng `tracking_events` (bảng append-only, không có `updated_at` hay `deleted_at`).

---

### External Interaction

- **Kafka**: Đẩy sự kiện tới topic `lms.tracking.events` phục vụ phân tích dữ liệu gian gian thực.

---

### Validation

#### Request Validation

- `X-Tenant-Id`, `X-User-Id`, `entityId`, `versionId` phải là UUID v4 hợp lệ.
- `eventType` thuộc Enum (`lesson.started`, `lesson.completed`, `block.entered`, `block.left`, `block.completed`).
- `source` thuộc Enum (`client_web`, `client_mobile`, `service:delivery`, `service:assignment`, `service:discussion`, `service:access`).
- Nếu `eventType` bắt đầu bằng `block.`, `blockId` và `nodePath` không được null.
- `idempotencyKey` không được để trống.

#### Business Validation

- Đảm bảo tính duy nhất của cặp `(tenant_id, idempotency_key)`.

#### Permission Validation

- Caller phải khớp với `X-User-Id` hoặc có chứng thư giao tiếp giữa các service.

---

## Part 3 — Data Interaction

### Operation 1: Chèn bản ghi sự kiện tracking mới

- **Operation Type**: `INSERT`
- **Target Table**: `tracking_events`
- **Conditions**: Khóa chính `id` tự sinh bằng UUID v4; ràng buộc duy nhất trên `(tenant_id, idempotency_key)`.
- **Expected Result**: Chèn thành công 1 bản ghi sự kiện append-only mới.

---

## Part 4 — Operational Notes

- **Idempotency**: Đảm bảo bằng ràng buộc duy nhất `uq_tracking_events_tenant_idempotency` trên `(tenant_id, idempotency_key)`.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi truy vấn SQL và ràng buộc DB.
- **Retry Strategy**: Client/Service có thể tự động phát lại sự kiện an toàn nhờ khóa idempotency.
- **Audit Logging**: Bản ghi `tracking_events` bản chất là nhật ký vết bất biến (immutable audit log).
- **Monitoring**: Theo dõi tốc độ ghi sự kiện (events/sec), tỷ lệ sự kiện ghi trùng (duplicates) và độ trễ đẩy tin sang Kafka.
- **Metrics**: `tracking_events_recorded_total`, `tracking_events_duplicate_total`.
- **Tracing**: Truyền dẫn `traceId` từ ngữ cảnh yêu cầu vào thuộc tính `context.trace_id` và message header Kafka.
