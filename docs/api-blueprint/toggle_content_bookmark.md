# API Blueprint — Toggle bookmark nội dung

Document generated following workflow `/03_generate_blueprint` and standards in `api-blueprint-generator.md`.

---

## Part 0 — Classification & Identity

- **API Name**: Toggle bookmark nội dung API
- **API Type**: Public
- **Module**: Result Management
- **Feature**: Content Bookmark Management
- **Description**: Cho phép học viên thêm mới hoặc hủy bỏ (toggle status) việc đánh dấu lưu lại (bookmark) một nội dung học tập/bài học/bài tập cụ thể. Nếu bookmark chưa tồn tại thì thực hiện tạo mới, nếu đã tồn tại thì thực hiện xóa/hủy đánh dấu.
- **Related Tables**: `content_bookmarks`
- **Related Services**: `lms-content-builder`

---

## Part 1 — API Contract

### Endpoint

- **HTTP Method**: `POST`
- **URL**: `/api/v1/result-service/bookmarks/toggle`
- **Content-Type**: `application/json`

---

### Request

#### Headers

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `X-Tenant-Id` | String | Yes | ID tenant sở hữu | Format UUID v4 |
| `X-User-Id` | String | Yes | ID học viên thực hiện bookmark | Format UUID v4 |

#### Path Variables

None.

#### Query Parameters

None.

#### Request Body

| Name | Type | Required | Description | Validation Rules |
|---|---|---|---|---|
| `contentId` | String | Yes | ID đối tượng nội dung cần bookmark | Format UUID v4 |
| `contentType` | String | Yes | Loại đối tượng nội dung | Enum: `LESSON`, `COURSEWARE`, `QUESTION`, `VIDEO` |
| `nodeId` | String | No | ID vị trí node tiến độ liên quan | Format UUID v4 nếu có |
| `note` | String | No | Ghi chú ngắn đi kèm khi bookmark | Max length 500 |

---

### Response

- **Success Status**: `200 OK`

#### Response Body

| Name | Type | Description |
|---|---|---|
| `contentId` | String | ID đối tượng nội dung |
| `userId` | String | ID học viên |
| `isBookmarked` | Boolean | Trạng thái bookmark hiện tại (`true` nếu vừa đánh dấu, `false` nếu vừa hủy) |
| `bookmarkId` | String | ID bản ghi bookmark (NULL nếu trạng thái vừa hủy) |
| `updatedAt` | String | Thời điểm thực hiện thao tác (ISO-8601 UTC) |

---

### Error Codes

| Error Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-400-001` | 400 | Dữ liệu request không hợp lệ | Malformed request body parameters. |
| `RES-401-001` | 401 | Thiếu thông tin xác thực | Authentication required. Missing or invalid tenant header. |
| `RES-403-001` | 403 | Không có quyền bookmark nội dung | Access denied for bookmark operation. |
| `RES-404-001` | 404 | Không tìm thấy đối tượng nội dung | Target content not found. |
| `RES-500-001` | 500 | Lỗi xử lý cơ sở dữ liệu hệ thống | An unexpected error occurred while toggling content bookmark. |

---

## Part 2 — Processing Specification

### Controller Layer

1. Đọc và kiểm tra HTTP Headers `X-Tenant-Id` và `X-User-Id`. Trả về `RES-401-001` nếu thiếu.
2. Kiểm tra tính hợp lệ của Request Body (`contentId`, `contentType`). Trả về `RES-400-001` nếu thiếu hoặc sai định dạng.
3. Delegate gọi Service layer `ToggleContentBookmarkService`.
4. Trả về trạng thái bookmark hiện tại kèm HTTP status `200 OK`.

---

### Service Layer

1. Xác thực quyền hạn học viên: Learner thao tác trên chính `userId` của mình trong tenant.
2. Kiểm tra sự tồn tại của bản ghi bookmark trong DB theo `(tenantId, userId, contentId)`.
3. **Nếu bản ghi đã tồn tại**:
   - Thực thi xóa bản ghi khỏi bảng `content_bookmarks`.
   - Trả về `isBookmarked = false` và `bookmarkId = null`.
4. **Nếu bản ghi chưa tồn tại**:
   - Thực thi chèn mới bản ghi vào bảng `content_bookmarks`.
   - Trả về `isBookmarked = true` và `bookmarkId` vừa sinh.

---

### Repository Layer

1. Truy vấn SELECT bản ghi bookmark theo `(tenant_id, user_id, content_id)`.
2. Thực thi INSERT nếu chưa có hoặc DELETE nếu đã tồn tại.

---

### External Interaction

None.

---

### Validation

#### Request Validation

- `X-Tenant-Id`, `X-User-Id`, `contentId` phải là UUID v4 hợp lệ.
- `contentType` phải thuộc danh sách Enum.

#### Business Validation

- Đảm bảo tính nhất quán dữ liệu bookmark duy nhất cho mỗi bộ khóa `(tenant_id, user_id, content_id)`.

#### Permission Validation

- Caller chỉ được bật/tắt bookmark của chính mình.

---

## Part 3 — Data Interaction

### Operation 1: Kiểm tra trạng thái bookmark hiện tại

- **Operation Type**: `SELECT`
- **Target Table**: `content_bookmarks`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND content_id = :contentId`
- **Expected Result**: Trả về bản ghi bookmark nếu tồn tại, ngược lại trả về rỗng.

---

### Operation 2: Thêm mới bản ghi bookmark

- **Operation Type**: `INSERT`
- **Target Table**: `content_bookmarks`
- **Conditions**: Khóa chính `id` tự sinh bằng UUID v4; ràng buộc duy nhất trên `(tenant_id, user_id, content_id)`.
- **Expected Result**: Chèn thành công 1 bản ghi bookmark mới.

---

### Operation 3: Xóa bản ghi bookmark

- **Operation Type**: `DELETE`
- **Target Table**: `content_bookmarks`
- **Conditions**: `tenant_id = :tenantId AND user_id = :userId AND content_id = :contentId`
- **Expected Result**: Xóa 1 bản ghi bookmark khỏi cơ sở dữ liệu.

---

## Part 4 — Operational Notes

- **Idempotency**: Thao tác toggle có tính chất đảo trạng thái; client gửi lặp lại sẽ đảo trạng thái giữa true và false.
- **Tenant Isolation**: Bắt buộc gắn điều kiện `tenant_id` trong mọi truy vấn SQL.
- **Retry Strategy**: Client nên kiểm tra trạng thái phản hồi trước khi phát lại request.
- **Audit Logging**: Không bắt buộc ghi log vết chi tiết cho thao tác bookmark.
- **Monitoring**: Theo dõi tổng số lượng nội dung được bookmark.
- **Metrics**: `result_service_bookmarks_toggled_total`.
- **Tracing**: Truyền dẫn trace context (`traceId`, `spanId`) qua các tầng.
