# API Blueprint — Truy vấn sự kiện tracking

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Truy vấn sự kiện tracking API
- **API Type**: Public / Internal
- **Module**: Tracking & Telemetry
- **Feature**: Tracking Events Query
- **Description**: Truy vấn lịch sử danh sách các sự kiện vòng đời học tập (`tracking_events`) có phân trang, hỗ trợ lọc theo học viên (`userId`), loại sự kiện (`eventType`), entity gốc (`entityKind`, `entityId`), version cụ thể (`versionId`), và nguồn phát sự kiện (`source`).
- **Related Tables**: `tracking_events`
- **Related Services**: `lms-delivery`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `GET`
- **URL**: `/api/v1/result-service/tracking-events`
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
| `userId` | String | No | Lọc theo ID học viên thực hiện | Format UUID v4 |
| `eventType` | String | No | Lọc theo loại sự kiện | Enum: `lesson.started`, `lesson.completed`, `block.entered`, `block.left`, `block.completed` |
| `entityKind` | String | No | Lọc theo loại entity gốc | Enum: `lesson`, `courseware` |
| `entityId` | String | No | Lọc theo ID entity gốc | Format UUID v4 |
| `versionId` | String | No | Lọc theo ID version cụ thể | Format UUID v4 |
| `source` | String | No | Lọc theo nguồn phát sự kiện | Enum: `client_web`, `client_mobile`, `service:delivery`, `service:assignment`, `service:discussion`, `service:access` |
| `page` | Integer | No | Trang hiện tại | Integer >= 1, Mặc định: 1 |
| `size` | Integer | No | Kích thước trang | Integer 1–100, Mặc định: 20 |
| `sort` | String | No | Trường sắp xếp | Mặc định: `occurredAt,desc` |

#### Request Body

None.

---

### Response

- **Success Status**: `200 OK`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `items` | Array of Objects | Danh sách các sự kiện tracking học tập |
| `items[].id` | String | ID sự kiện tracking (`tracking_events.id`) |
| `items[].tenantId` | String | ID tenant sở hữu |
| `items[].userId` | String | ID học viên thực hiện |
| `items[].eventType` | String | Loại sự kiện vòng đời |
| `items[].entityKind` | String | Loại entity gốc (`lesson`, `courseware`) |
| `items[].entityId` | String | ID entity gốc |
| `items[].versionId` | String | ID version cụ thể |
| `items[].versionNo` | Integer | Số phiên bản |
| `items[].language` | String | Mã ngôn ngữ |
| `items[].blockId` | String | ID block trong snapshot (nếu có) |
| `items[].nodePath` | String | Path từ root tới block (nếu có) |
| `items[].occurredAt` | String | Thời điểm xảy ra thực tế (ISO-8601 UTC) |
| `items[].receivedAt` | String | Thời điểm hệ thống nhận sự kiện (ISO-8601 UTC) |
| `items[].source` | String | Nguồn phát sự kiện |
| `items[].idempotencyKey` | String | Khóa chống ghi trùng |
| `items[].payload` | Object | Dữ liệu động theo eventType |
| `items[].context` | Object | Metadata môi trường |
| `page` | Integer | Số trang hiện tại |
| `size` | Integer | Kích thước trang |
| `total` | Long | Tổng số sự kiện thỏa điều kiện |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Meaning |
|---|---|---|---|
| `RES-400-001` | 400 | Query parameter không hợp lệ | Malformed or out of range query parameters. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền truy vấn danh sách sự kiện này | Access denied to tracking event logs. |
| `RES-500-001` | 500 | Lỗi hệ thống khi truy vấn cơ sở dữ liệu | An unexpected error occurred while listing tracking events. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Kiểm tra HTTP Header `X-Tenant-Id`. Trả về `RES-401-001` nếu thiếu.
2. Đọc và kiểm tra các Query Parameters (`userId`, `eventType`, `entityKind`, `entityId`, `versionId`, `source`, `page`, `size`, `sort`). Trả về `RES-400-001` nếu tham số không hợp lệ.
3. Delegate gọi Service layer `ListTrackingEventsService`.
4. Wrap kết quả thành paginated response DTO và trả về HTTP status `200 OK`.

---

### Service Layer

1. Kiểm tra phân quyền truy cập: Học viên thông thường chỉ được xem sự kiện của chính mình (`userId`). Quản trị viên/Giảng viên có quyền xem toàn bộ sự kiện trong tenant.
2. Gọi Repository layer thực thi câu truy vấn tìm kiếm sự kiện có lọc và phân trang.
3. Đếm tổng số bản ghi thỏa mãn điều kiện để trả về `total`.
4. Trả về đối tượng danh sách kết quả.

---

### Repository Layer

1. Xây dựng câu lệnh truy vấn động dựa trên các bộ lọc: `tenant_id = :tenantId`, `user_id`, `event_type`, `entity_kind`, `entity_id`, `version_id`, `source`.
2. Áp dụng sắp xếp theo `occurred_at DESC` và pagination limit/offset.

---

### External Interaction

None.

---

### Validation

#### Request Validation

- `X-Tenant-Id` phải là UUID v4 hợp lệ.
- `userId`, `entityId`, `versionId` phải là UUID v4 hợp lệ nếu được truyền.
- `page` >= 1, `size` trong khoảng từ 1 đến 100.

#### Business Validation

- Mọi câu truy vấn đều phải giới hạn trong phạm vi `tenant_id`.

#### Permission Validation

- Caller không phải admin/instructor bắt buộc phải gắn điều kiện `userId` trùng với mình.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn danh sách sự kiện tracking học tập

- **Operation Type**: `SELECT`
- **Target Table**: `tracking_events`
- **Conditions**: `tenant_id = :tenantId AND (:userId IS NULL OR user_id = :userId) AND (:eventType IS NULL OR event_type = :eventType) AND (:entityKind IS NULL OR entity_kind = :entityKind) AND (:entityId IS NULL OR entity_id = :entityId) AND (:versionId IS NULL OR version_id = :versionId) AND (:source IS NULL OR source = :source)`
- **Expected Result**: Trả về danh sách bản ghi sự kiện tracking sắp xếp theo `occurred_at DESC` với LIMIT `:size` OFFSET `(:page - 1) * :size`.
- **Performance Notes**: Tận dụng các chỉ mục:
  - `ix_tracking_events_user_occurred (tenant_id, user_id, occurred_at)`
  - `ix_tracking_events_entity_version_occurred (tenant_id, entity_kind, entity_id, version_id, occurred_at)`
  - `ix_tracking_events_type_occurred (tenant_id, event_type, occurred_at)`

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác truy vấn danh sách (`GET`), có tính idempotent tuyệt đối.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi truy vấn SQL.
- **Retry Strategy**: Client có thể tự động thử lại an toàn khi gặp sự cố mạng.
- **Audit Logging**: Không yêu cầu ghi log audit cho thao tác xem danh sách sự kiện.
- **Monitoring**: Theo dõi thời gian truy vấn danh sách sự kiện và tốc độ phản hồi trên tập dữ liệu lớn.
- **Metrics**: `tracking_events_list_query_seconds`, `tracking_events_list_query_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) xuyên suốt các tầng.
