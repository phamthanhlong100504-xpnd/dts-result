# API Blueprint — Đánh giá tiêu chí hoàn thành

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Đánh giá tiêu chí hoàn thành API
- **API Type**: Internal / Public
- **Module**: Result Management
- **Feature**: Criteria Evaluation Processing
- **Description**: Thực hiện kiểm tra, đo lường và đánh giá lại kết quả hoàn thành tiêu chí học tập (`user_criteria_results`) cho học viên dựa trên tiến độ thực tế, thời gian học hoặc điểm số bài làm. Nếu giá trị đo lường đạt ngưỡng `targetValue`, tự động cập nhật `status = MET` và lưu mốc thời gian `metAt`.
- **Related Tables**: `user_criteria_results`, `criterias`, `user_content_results`
- **Related Services**: `lms-content-builder`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `POST`
- **URL**: `/api/v1/result-service/users/{userId}/criteria-results/evaluate`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant sở hữu | Format UUID v4 |
| `X-User-Id` | String | Yes | ID người dùng gửi yêu cầu | Format UUID v4 |

#### Path Variables

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `userId` | String | Yes | ID học viên cần đánh giá tiêu chí | Format UUID v4 |

#### Query Parameters

None.

#### Request Body

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `nodeId` | String | No | Lọc theo ID node tiến độ áp dụng (nếu có) | Format UUID v4 nếu có |
| `criteriaId` | String | No | ID tiêu chí cụ thể cần đánh giá (nếu chỉ định) | Format UUID v4 nếu có |
| `triggerSource` | String | Yes | Nguồn kích hoạt đánh giá | Enum: `PROGRESS_UPDATE`, `ATTEMPT_SUBMITTED`, `MANUAL_RECALCULATE` |

---

### Response

- **Success Status**: `200 OK`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `userId` | String | ID học viên được đánh giá |
| `evaluatedCount` | Integer | Tổng số bản ghi tiêu chí đã được đánh giá |
| `metCount` | Integer | Số lượng tiêu chí đạt trạng thái `MET` |
| `results` | Array of Objects | Danh sách kết quả đánh giá chi tiết từng tiêu chí |
| `results[].criteriaId` | String | ID tiêu chí đánh giá |
| `results[].currentValue` | Decimal | Giá trị hiện tại đo lường được |
| `results[].targetValue` | Decimal | Mức giá trị mục tiêu cần đạt |
| `results[].status` | String | Trạng thái mới (`NOT_MET`, `MET`) |
| `results[].isNewlyMet` | Boolean | Đánh dấu nếu đây là lần đầu tiên đạt tiêu chí này |
| `evaluatedAt` | String | Thời điểm hoàn tất đánh giá (ISO-8601 UTC) |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Path variable `userId` hoặc Body không hợp lệ | Malformed request parameters or invalid payload. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền đánh giá tiêu chí người dùng | Access denied for criteria evaluation. |
| `RES-404-001` | 404 | Không tìm thấy cấu hình tiêu chí hoặc người dùng | Criteria configuration or user not found. |
| `RES-500-001` | 500 | Lỗi xử lý đánh giá hệ thống | An unexpected error occurred while evaluating criteria results. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Kiểm tra HTTP Headers `X-Tenant-Id` và `X-User-Id`. Trả về `RES-401-001` nếu thiếu.
2. Kiểm tra tính hợp lệ của Path variable `userId` và các trường trong Request Body (`triggerSource`). Trả về `RES-400-001` nếu sai.
3. Delegate gọi Service layer `EvaluateUserCriteriaService`.
4. Trả về kết quả đánh giá chi tiết kèm HTTP status `200 OK`.

---

### Service Layer

1. Xác thực quyền hạn truy cập của người gửi yêu cầu đối với tenant.
2. Truy vấn danh sách các tiêu chí áp dụng cho học viên theo `nodeId` hoặc `criteriaId` từ bảng `criterias`.
3. Đọc dữ liệu đo lường thực tế từ `user_content_results` hoặc `user_content_attempts` (tổng thời gian học, số lượt nộp bài, điểm số bài thi).
4. Tính toán giá trị hiện tại `currentValue` cho từng tiêu chí.
5. So sánh `currentValue >= targetValue`:
   - Nếu đạt: Đặt `status = MET`, cập nhật `metAt = NOW()` (nếu trước đó là `NOT_MET` thì đánh dấu `isNewlyMet = true`).
   - Nếu chưa đạt: Đặt `status = NOT_MET`.
6. Thực thi lưu/cập nhật danh sách bản ghi `user_criteria_results`.
7. Nếu có tiêu chí mới đạt (`isNewlyMet = true`), phát sự kiện Outbox `criteria.met` để thông báo thưởng huy hiệu/chứng chỉ.
8. Trả về danh sách kết quả đánh giá.

---

### Repository Layer

1. Truy vấn danh sách cấu hình tiêu chí từ `criterias`.
2. Truy vấn/Chèn/Cập nhật dữ liệu `user_criteria_results`.

---

### External Interaction

- **Kafka**: Đẩy sự kiện tới topic `lms.result.criteria-met` khi học viên đạt tiêu chí mới.

---

### Validation

#### Request Validation

- `X-Tenant-Id`, `X-User-Id` và `userId` phải là UUID v4 hợp lệ.
- `triggerSource` thuộc Enum danh sách nguồn kích hoạt.

#### Business Validation

- Mốc thời gian `metAt` giữ nguyên thời điểm đạt đầu tiên, không bị ghi đè ở các lần đánh giá sau.

#### Permission Validation

- Caller phải thuộc phạm vi tenant và có quyền thực thi đánh giá.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn danh sách tiêu chí cần đánh giá

- **Operation Type**: `SELECT`
- **Target Table**: `criterias`
- **Conditions**: `tenant_id = :tenantId AND (:criteriaId IS NULL OR id = :criteriaId)`
- **Expected Result**: Trả về tập hợp các quy tắc tiêu chí đánh giá.

---

### Operation 2: Cập nhật / Chèn kết quả đánh giá tiêu chí người dùng

- **Operation Type**: `INSERT` / `UPDATE`
- **Target Table**: `user_criteria_results`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND criteria_id = :criteriaId`
- **Expected Result**: Cập nhật `current_value = :currentValue`, `status = :status`, `met_at = COALESCE(met_at, :metAt)`, `updated_at = NOW()`.

---

## Part 4 — Operational Notes

- **Idempotency**: Việc đánh giá tiêu chí có tính chất idempotent (cùng tập dữ liệu tiến độ sẽ trả ra kết quả đánh giá trạng thái bất biến).
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi truy vấn SQL.
- **Retry Strategy**: Tiến trình có thể chạy lại an toàn mà không làm sai lệch mốc thời gian đạt `metAt`.
- **Audit Logging**: Ghi nhật ký vết khi tiêu chí chuyển trạng thái sang `MET`.
- **Monitoring**: Theo dõi thời gian thực thi đánh giá tiêu chí và số lượng tiêu chí đạt mới.
- **Metrics**: `result_service_criteria_evaluated_total`, `result_service_criteria_met_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) xuyên suốt quá trình xử lý.
