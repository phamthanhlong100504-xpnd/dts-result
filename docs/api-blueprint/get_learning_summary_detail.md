# API Blueprint: Lấy chi tiết tiến độ tổng hợp

## Part 0: API Identity

- **API Name**: Get Learning Summary Detail
- **API Type**: Internal / Public
- **Module**: Result & Tracking Service
- **Feature**: Learning Summary Detail
- **Description**: Truy vấn thông tin tiến độ tổng hợp chi tiết của 1 bản ghi theo ID.
- **Related Tables**: `learning_summaries`

---

## Part 1: API Contract

### Endpoint
`GET /api/v1/result-service/learning-summaries/{id}`

### HTTP Method
`GET`

### Request Headers
| Header Key | Type | Required | Description |
|---|---|---|---|
| `X-User-Id` | UUID | Mandatory | ID người dùng đang gửi request |
| `X-User-Roles` | String | Optional | Vai trò người dùng (ví dụ: `ROLE_STUDENT`, `ROLE_TEACHER`, `ROLE_ADMIN`) |

### Path Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| `id` | UUID | Mandatory | ID bản ghi learning_summary |

### Response (200 OK)
Trả về object `LearningSummaryResponse` chi tiết.

### Error Codes
| Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-401-001` | 401 Unauthorized | Thiếu header định danh | Authentication required. Missing header: X-User-Id |
| `RES-403-001` | 403 Forbidden | Không có quyền xem tiến độ của người khác | You do not have permission to access another user's result. |
| `RES-404-002` | 404 Not Found | Không tìm thấy bản ghi | Learning summary not found with ID: {id} |

---

## Part 2: Processing Spec

- **Controller**: Trích xuất header `X-User-Id`, `X-User-Roles` và path `id`, gọi `getLearningSummaryDetail(callerUserId, callerRoles, id)`.
- **Service**: 
  1. Tìm bản ghi theo ID trong bảng `learning_summaries`. Trả về `RES-404-002` nếu không tồn tại.
  2. **Ownership Validation**: Kiểm tra `callerUserId == entity.userId` hoặc `callerRoles` chứa `ROLE_ADMIN`/`ROLE_TEACHER`. Nếu không thỏa mãn ném `RES-403-001`.
  3. Trả về thông tin chi tiết.
- **Repository**: Query `findById(id)`.
