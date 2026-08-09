# API Blueprint: Overview & Statistics (Stage 7)

## Tổng quan
Cung cấp các số liệu tổng hợp (Aggregation) để hiển thị trên Dashboard của người dùng, báo cáo cá nhân và vẽ các biểu đồ thống kê học tập.

---

## 1. Get Overview (Tổng quan Dashboard)
**Endpoint:** `GET /v1/results/me/overview`
**Authentication:** Yêu cầu JWT (Lấy `userId` từ token)

### Business Rules
- Chỉ query bảng `learning_summaries`.
- Đếm tổng số Programs, Chapters, Lessons đã hoàn thành.
- Tính điểm trung bình (`averageScore`), điểm cao nhất (`bestScore`).
- Tính tổng thời gian học và số lần thử (attempts).

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

## 2. Get Statistics (Thống kê biểu đồ)
**Endpoint:** `GET /v1/results/me/statistics`
**Authentication:** Yêu cầu JWT

### Query Parameters
| Tham số | Bắt buộc | Mặc định | Mô tả |
| :--- | :---: | :--- | :--- |
| `from` | Không | `now - 30 days` | Ngày bắt đầu (ISO Date) |
| `to` | Không | `now` | Ngày kết thúc (ISO Date) |
| `interval` | Không | `DAY` | Nhóm theo khoảng thời gian: `DAY`, `WEEK`, `MONTH` |

### Business Rules
- Khai thác dữ liệu từ bảng `learning_results` (cho số điểm, số attempts, thời gian theo từng ngày) và `learning_summaries` (đối với hoàn thành).
- Giới hạn tối đa 365 ngày để đảm bảo Performance (Từ chối request nếu `from` -> `to` > 365 days).
- Trả về danh sách chuỗi dữ liệu (Trend) theo ngày.

### Response (200 OK)
```json
{
  "scoreTrend": [
    {
      "date": "2026-08-01",
      "averageScore": 82.0
    }
  ],
  "studyTimeTrend": [
    {
      "date": "2026-08-01",
      "durationSeconds": 5400
    }
  ],
  "attemptTrend": [
    {
      "date": "2026-08-01",
      "attempts": 5
    }
  ],
  "completionTrend": [
    {
      "date": "2026-08-01",
      "completed": 3
    }
  ]
}
```
