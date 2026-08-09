# API Blueprint: Internal Services (Stage 8)

## Tổng quan
Cung cấp API cho phép các service nội bộ khác (như Learning Service, Course Service, Reporting Service, Notification Service) truy vấn dữ liệu từ Result Service thông qua giao tiếp Service-to-Service.

---

## 1. Internal Overview (Tổng quan một người dùng)
**Endpoint:** `GET /internal/results/users/{userId}/overview`
**Authentication:** Gateway Internal Authentication (API Gateway hoặc hạ tầng mạng sẽ chặn IP ngoài. Trong Spring Security cấu hình `permitAll()`).

### Business Rules
- Bỏ qua khâu kiểm tra quyền sở hữu (Ownership check). Trả về Overview của bất kỳ user nào được yêu cầu.
- Thường dùng cho Admin/Reporting.

### Path Variables
- `userId` (UUID): ID của người dùng cần truy vấn.

### Response (200 OK)
```json
{
  "completedPrograms": 2,
  "inProgressPrograms": 1,
  "completedChapters": 12,
  "completedLessons": 48,
  "averageScore": 84.5,
  "bestScore": 96.0,
  "totalLearningTimeSeconds": 125400,
  "totalAttempts": 37,
  "lastActivityAt": "2026-08-04T10:00:00Z"
}
```

---

## 2. Internal Summary List (Danh sách tiến độ một người dùng)
**Endpoint:** `GET /internal/results/users/{userId}/summaries`
**Authentication:** Gateway Internal Authentication

### Query Parameters
| Tham số | Bắt buộc | Mô tả |
| :--- | :---: | :--- |
| `targetType` | Không | Lọc theo loại đối tượng (EXAM, CHAPTER, ...) |
| `status` | Không | Lọc theo trạng thái (COMPLETED, IN_PROGRESS, ...) |

### Business Rules
- Bỏ qua quyền sở hữu. Trả danh sách (list, không phân trang) tất cả projection của một user.
- Thường dùng cho Learning Service để lấy nhanh tiến độ mở khóa các lesson tiếp theo.

### Response (200 OK)
```json
[
  {
    "targetType": "CHAPTER",
    "targetId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "IN_PROGRESS",
    "progress": 60.0,
    "attemptCount": 2,
    "bestScore": 88.0
  }
]
```
