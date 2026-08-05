# API Blueprint — Lấy kết quả tiêu chí user

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Lấy kết quả tiêu chí user API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Criteria Completion Results Query
- **Description**: Truy vấn thông tin và kết quả đánh giá mức độ đạt tiêu chí học tập (`user_criteria_results`) của học viên theo `userId`, bao gồm điểm số tiêu chí đạt được, giá trị hiện tại, trạng thái hoàn thành (`MET`, `NOT_MET`) và thời điểm hoàn thành tiêu chí.
- **Related Tables**: `user_criteria_results`, `criterias`
- **Related Services**: `lms-content-builder`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `GET`
- **URL**: `/api/v1/result-service/users/{userId}/criteria-results`
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
| `userId` | String | Yes | ID học viên cần lấy kết quả tiêu chí | Format UUID v4 |

#### Query Parameters

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `criteriaId` | String | No | Lọc theo ID tiêu chí cụ thể | Format UUID v4 |
| `status` | String | No | Lọc theo trạng thái đạt | Enum: `NOT_MET`, `MET` |
| `page` | Integer | No | Trang hiện tại | Integer >= 1, Mặc định: 1 |
| `size` | Integer | No | Kích thước trang | Integer 1–100, Mặc định: 20 |

#### Request Body

None.

---

### Response

- **Success Status**: `200 OK`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `items` | Array of Objects | Danh sách kết quả hoàn thành tiêu chí |
| `items[].id` | String | ID bản ghi kết quả tiêu chí (`user_criteria_results.id`) |
| `items[].tenantId` | String | ID tenant |
| `items[].userId` | String | ID học viên |
| `items[].criteriaId` | String | ID tiêu chí đánh giá (`criterias.id`) |
| `items[].nodeId` | String | ID node tiến độ áp dụng tiêu chí (nếu có) |
| `items[].currentValue` | Decimal | Giá trị hiện tại đo lường được của học viên |
| `items[].targetValue` | Decimal | Giá trị mục tiêu cần đạt của tiêu chí |
| `items[].status` | String | Trạng thái đạt tiêu chí (`NOT_MET`, `MET`) |
| `items[].metAt` | String | Thời điểm đạt tiêu chí (ISO-8601 UTC, NULL nếu chưa đạt) |
| `items[].createdAt` | String | Thời điểm tạo bản ghi |
| `items[].updatedAt` | String | Thời điểm đánh giá gần nhất |
| `page` | Integer | Số trang hiện tại |
| `size` | Integer | Kích thước trang |
| `total` | Long | Tổng số bản ghi thỏa điều kiện |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Path variable `userId` hoặc Query parameter không hợp lệ | Malformed parameters or invalid pagination range. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền xem kết quả tiêu chí của user này | Access denied to user criteria result resources. |
| `RES-404-001` | 404 | Không tìm thấy kết quả tiêu chí | User criteria results not found. |
| `RES-500-001` | 500 | Lỗi hệ thống khi truy vấn | An unexpected error occurred while fetching criteria results. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Kiểm tra HTTP Header `X-Tenant-Id` và Path variable `userId`. Trả về `RES-401-001` hoặc `RES-400-001` nếu vi phạm.
2. Đọc các tham số Query Parameters (`criteriaId`, `status`, `page`, `size`).
3. Delegate gọi Service layer `GetUserCriteriaResultsService`.
4. Wrap kết quả thu được vào response DTO và trả về HTTP `200 OK`.

---

### Service Layer

1. Kiểm tra phân quyền: Caller là chính học viên (`userId`) hoặc vai trò Quản trị/Giảng viên thuộc tenant.
2. Gọi Repository layer truy vấn danh sách bản ghi `user_criteria_results` khớp với `tenant_id`, `user_id`, `criteria_id`, `status`.
3. Đếm tổng số bản ghi thỏa mãn để hỗ trợ phân trang.
4. Trả về đối tượng kết quả phân trang.

---

### Repository Layer

1. Thực thi câu lệnh SQL SELECT trên bảng `user_criteria_results` lọc theo `tenant_id = :tenantId AND user_id = :userId`.
2. Bổ sung các điều kiện lọc `criteria_id = :criteriaId` và `status = :status` nếu có.

---

### External Interaction

None.

---

### Validation

#### Request Validation

- `X-Tenant-Id` và `userId` phải là UUID v4 hợp lệ.
- `page` >= 1, `size` từ 1 đến 100.
- `status` thuộc Enum (`NOT_MET`, `MET`) nếu được truyền.

#### Business Validation

- Mọi truy vấn bắt buộc chỉ định phạm vi `tenant_id` và `user_id`.

#### Permission Validation

- Caller phải có quyền xem dữ liệu của `userId` tương ứng.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn danh sách kết quả tiêu chí của học viên

- **Operation Type**: `SELECT`
- **Target Table**: `user_criteria_results`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND (:criteriaId IS NULL OR criteria_id = :criteriaId) AND (:status IS NULL OR status = :status)`
- **Expected Result**: Trả về danh sách bản ghi kết quả tiêu chí học viên với LIMIT `:size` OFFSET `(:page - 1) * :size`.

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác truy vấn danh sách (`GET`), có tính chất idempotent tuyệt đối.
- **Tenant Isolation**: Bắt buộc lọc theo `tenant_id` trong mọi truy vấn SQL.
- **Retry Strategy**: Client có thể tự động thử lại an toàn khi ngắt kết nối mạng.
- **Audit Logging**: Không yêu cầu ghi log audit cho thao tác đọc kết quả tiêu chí.
- **Monitoring**: Theo dõi thời gian phản hồi truy vấn kết quả tiêu chí hoàn thành.
- **Metrics**: `result_service_criteria_results_query_seconds`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) xuyên suốt các tầng.
