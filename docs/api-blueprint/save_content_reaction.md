# API Blueprint — Thả reaction nội dung

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Thả reaction nội dung API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Content Reaction Management
- **Description**: Cho phép học viên bộc lộ cảm xúc/đánh giá (`content_reactions`) đối với một bài học, tài liệu hoặc câu hỏi (ví dụ: `LIKE`, `LOVE`, `HELPFUL`, `DISLIKE`). Nếu học viên thả cùng loại reaction sẽ thực hiện xóa/hủy, nếu khác loại sẽ thực hiện cập nhật loại mới.
- **Related Tables**: `content_reactions`
- **Related Services**: `lms-content-builder`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `POST`
- **URL**: `/api/v1/result-service/reactions`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant sở hữu | Format UUID v4 |
| `X-User-Id` | String | Yes | ID học viên thả reaction | Format UUID v4 |

#### Path Variables

None.

#### Query Parameters

None.

#### Request Body

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `contentId` | String | Yes | ID đối tượng nội dung tương tác | Format UUID v4 |
| `contentType` | String | Yes | Loại đối tượng nội dung | Enum: `LESSON`, `COURSEWARE`, `QUESTION`, `VIDEO` |
| `reactionType` | String | Yes | Loại cảm xúc bộc lộ | Enum: `LIKE`, `LOVE`, `HELPFUL`, `DISLIKE` |

---

### Response

- **Success Status**: `200 OK`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `contentId` | String | ID đối tượng nội dung |
| `userId` | String | ID học viên |
| `reactionType` | String | Loại cảm xúc hiện tại (NULL nếu vừa bị hủy) |
| `isReacted` | Boolean | Trạng thái cờ đánh dấu (`true` nếu đang thả reaction, `false` nếu vừa bị xóa/hủy) |
| `updatedAt` | String | Thời điểm thực hiện thao tác (ISO-8601 UTC) |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Dữ liệu reaction không hợp lệ | Malformed request parameters or invalid reaction type enum. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền thao tác reaction | Access denied for reaction operation. |
| `RES-404-001` | 404 | Không tìm thấy đối tượng nội dung | Target content not found. |
| `RES-500-001` | 500 | Lỗi xử lý cơ sở dữ liệu hệ thống | An unexpected error occurred while saving content reaction. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Đọc và kiểm tra HTTP Headers `X-Tenant-Id` và `X-User-Id`. Trả về `RES-401-001` nếu thiếu.
2. Kiểm tra tính hợp lệ của Request Body (`contentId`, `contentType`, `reactionType`). Trả về `RES-400-001` nếu sai.
3. Delegate gọi Service layer `SaveContentReactionService`.
4. Trả về trạng thái reaction mới nhất kèm HTTP status `200 OK`.

---

### Service Layer

1. Xác thực quyền hạn: Học viên thao tác trên chính `userId` của mình trong tenant.
2. Truy vấn bản ghi reaction hiện tại từ DB theo `(tenantId, userId, contentId)`.
3. **Nếu bản ghi chưa tồn tại**:
   - Thực thi chèn mới bản ghi với `reaction_type = reactionType`.
   - Trả về `isReacted = true` và `reactionType`.
4. **Nếu bản ghi đã tồn tại**:
   - Nếu `reaction_type` cũ trùng với `reactionType` mới: Thực thi xóa bản ghi (hủy reaction), trả về `isReacted = false` và `reactionType = null`.
   - Nếu `reaction_type` cũ khác `reactionType` mới: Thực thi cập nhật `reaction_type = reactionType`, trả về `isReacted = true` và `reactionType` mới.

---

### Repository Layer

1. Truy vấn `SELECT` bản ghi reaction theo `(tenant_id, user_id, content_id)`.
2. Thực thi `INSERT`, `UPDATE` hoặc `DELETE` tùy theo kịch bản logic nghiệp vụ.

---

### External Interaction

None.

---

### Validation

#### Request Validation

- `X-Tenant-Id`, `X-User-Id`, `contentId` phải là UUID v4 hợp lệ.
- `reactionType` thuộc danh sách Enum (`LIKE`, `LOVE`, `HELPFUL`, `DISLIKE`).

#### Business Validation

- Đảm bảo duy nhất tối đa 1 bản ghi reaction trên mỗi bộ khóa `(tenant_id, user_id, content_id)`.

#### Permission Validation

- Caller chỉ được quản lý reaction của chính mình.

---

## Part 3 — Data Interaction

### Operation 1: Truy vấn trạng thái reaction hiện tại của học viên

- **Operation Type**: `SELECT`
- **Target Table**: `content_reactions`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND content_id = :contentId`
- **Expected Result**: Trả về bản ghi reaction nếu đã tồn tại.

---

### Operation 2: Chèn bản ghi reaction mới

- **Operation Type**: `INSERT`
- **Target Table**: `content_reactions`
- **Conditions**: Khóa chính `id` tự sinh bằng UUID v4; ràng buộc duy nhất trên `(tenant_id, user_id, content_id)`.
- **Expected Result**: Chèn thành công 1 bản ghi reaction mới.

---

### Operation 3: Cập nhật loại reaction

- **Operation Type**: `UPDATE`
- **Target Table**: `content_reactions`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND content_id = :contentId`
- **Expected Result**: Cập nhật `reaction_type = :reactionType`, `updated_at = NOW()`.

---

### Operation 4: Xóa bản ghi reaction (hủy thả cảm xúc)

- **Operation Type**: `DELETE`
- **Target Table**: `content_reactions`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND content_id = :contentId`
- **Expected Result**: Xóa 1 bản ghi reaction khỏi DB.

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác có tính chất cập nhật/đảo trạng thái xác định.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi thao tác SQL.
- **Retry Strategy**: Client có thể tự động thử lại khi gặp sự cố kết nối mạng.
- **Audit Logging**: Không yêu cầu ghi log vết chi tiết cho thao tác thả reaction.
- **Monitoring**: Theo dõi tổng số lượng reaction phân theo từng loại (`LIKE`, `LOVE`, `HELPFUL`).
- **Metrics**: `result_service_reactions_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) xuyên suốt các tầng.
