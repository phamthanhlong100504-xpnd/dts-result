# API Blueprint: Lấy danh sách tiến độ tổng hợp của người dùng

## Part 0: API Identity

- **API Name**: Get User Learning Summaries
- **API Type**: Internal / Public
- **Module**: Result & Tracking Service
- **Feature**: User Progress Summaries
- **Description**: Trả về toàn bộ danh sách tiến độ học tập tổng hợp của người dùng theo các đối tượng học.
- **Related Tables**: `learning_summaries`

---

## Part 1: API Contract

### Endpoint
`GET /api/v1/result-service/users/{userId}/learning-summaries`

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
| `userId` | UUID | Mandatory | ID người dùng sở hữu dữ liệu |

### Response (200 OK)
Trả về danh sách `List<LearningSummaryResponse>`.

### Error Codes
| Code | HTTP Status | Business Meaning | Client Message |
|---|---|---|---|
| `RES-401-001` | 401 Unauthorized | Thiếu header định danh | Authentication required. Missing header: X-User-Id |
| `RES-403-001` | 403 Forbidden | Không có quyền xem tiến độ của người khác | You do not have permission to access another user's result. |

---

## Part 2: Processing Spec

- **Controller**: Trích xuất header `X-User-Id`, `X-User-Roles` và path `userId`, gọi `getUserLearningSummaries(callerUserId, callerRoles, userId)`.
- **Service**: 
  1. **Ownership Validation**: Kiểm tra `callerUserId == userId` hoặc `callerRoles` chứa `ROLE_ADMIN`/`ROLE_TEACHER`. Nếu không thỏa mãn ném `RES-403-001`.
  2. Truy vấn danh sách bản ghi tổng hợp của user từ bảng `learning_summaries`.
- **Repository**: Query `findByUserId(userId)`.

---

## Part 3: Data Interaction

- **SELECT**: `SELECT s FROM LearningSummaryEntity s WHERE s.userId = :userId`
