# API Blueprint: Lấy chi tiết kết quả lượt thực hiện

## Part 0: API Identity

- **API Name**: Get Learning Result Detail
- **API Type**: Internal / Public
- **Module**: Result & Tracking Service
- **Feature**: Learning Result Detail
- **Description**: Truy vấn thông tin chi tiết của 1 lượt nộp/phiên học theo ID.
- **Related Tables**: `learning_results`

---

## Part 1: API Contract

### Endpoint
`GET /api/v1/result-service/learning-results/{id}`

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
| `id` | UUID | Mandatory | ID bản ghi learning_result |

### Response (200 OK)
Trả về object `LearningResultResponse` chi tiết.

### Error Codes
| Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-401-001` | 401 Unauthorized | Thiếu header định danh | Authentication required. Missing header: X-User-Id |
| `RES-403-001` | 403 Forbidden | Không có quyền truy cập dữ liệu của người khác | You do not have permission to access another user's result. |
| `RES-404-001` | 404 Not Found | Không tìm thấy bản ghi | Learning result not found with ID: {id} |

---

## Part 2: Processing Spec

- **Controller**: Trích xuất header `X-User-Id`, `X-User-Roles` và path variable `id`, gọi `getLearningResultDetail(callerUserId, callerRoles, id)`.
- **Service**: 
  1. Tìm bản ghi theo ID trong bảng `learning_results`. Trả về `RES-404-001` nếu không tồn tại.
  2. **Ownership Validation**: Kiểm tra `callerUserId == entity.userId` hoặc `callerRoles` chứa `ROLE_ADMIN`/`ROLE_TEACHER`. Nếu không thỏa mãn ném `RES-403-001`.
  3. Trả về thông tin chi tiết.
- **Repository**: Query `findById(id)`.

---

## Part 3: Data Interaction

- **SELECT**: `SELECT r FROM LearningResultEntity r WHERE r.id = :id`
