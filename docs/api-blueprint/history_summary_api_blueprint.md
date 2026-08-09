# API Blueprint: History & Summary (Stage 5 & 6)

## Tổng quan
Tài liệu này định nghĩa chi tiết các REST APIs giao tiếp với Frontend hoặc các dịch vụ khác để truy vấn (Read-only) dữ liệu học tập (History) và số liệu tổng hợp (Summary/Progress) từ Result Service.

Tất cả các API bên dưới đều yêu cầu xác thực bằng **JWT Bearer Token**. Tham số `userId` được trích xuất trực tiếp từ Token tại Backend (SecurityContext) nhằm đảm bảo an toàn, không nhận từ đường dẫn hay payload của client.

---

## Stage 5: History APIs
*Các API phục vụ truy vấn lịch sử từng lần làm bài / học tập của người dùng, lấy dữ liệu từ bảng `learning_results`.*

### 1. Lấy danh sách lịch sử (History List)
**Endpoint:** `GET /v1/results/me/history`
**Mô tả:** Lấy danh sách các bài làm của người dùng, hỗ trợ phân trang và bộ lọc linh hoạt.

**Query Parameters:**
| Tham số | Bắt buộc | Kiểu | Mô tả |
| :--- | :---: | :--- | :--- |
| `targetType` | Không | String | Loại đối tượng học (EXAM, CHAPTER, LESSON, ...) |
| `targetId` | Không | UUID | ID của đối tượng học |
| `result` | Không | String | Trạng thái (PASSED, FAILED, COMPLETED, ...) |
| `from` | Không | Datetime | Lọc từ thời điểm (ISO Format) |
| `to` | Không | Datetime | Lọc đến thời điểm (ISO Format) |
| `page` | Không | Integer | Trang hiện tại (Mặc định: 0) |
| `size` | Không | Integer | Số bản ghi/trang (Mặc định: 20) |
| `sort` | Không | String | (Mặc định: `completedAt,desc`) |

**Response (200 OK - Phân trang):**
```json
{
  "content": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "targetType": "EXAM",
      "targetId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "attemptNo": 1,
      "result": "PASSED",
      "score": 90.0,
      "maxScore": 100.0,
      "progress": 100.0,
      "durationSeconds": 1200,
      "completedAt": "2026-08-09T10:00:00Z"
    }
  ],
  "pageable": { ... },
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Lấy chi tiết lịch sử (History Detail)
**Endpoint:** `GET /v1/results/me/history/{resultId}`
**Mô tả:** Lấy chi tiết một bài làm cụ thể bao gồm cả metadata và snapshot chấm điểm.

**Path Variables:**
- `resultId` (UUID): ID của bản ghi learning_result.

**Response (200 OK):**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "targetType": "EXAM",
  "targetId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "attemptNo": 1,
  "result": "PASSED",
  "score": 90.0,
  "maxScore": 100.0,
  "progress": 100.0,
  "durationSeconds": 1200,
  "startedAt": "2026-08-09T09:40:00Z",
  "completedAt": "2026-08-09T10:00:00Z",
  "resultSnapshot": {
    "correctQuestions": 45,
    "totalQuestions": 50
  },
  "metadata": {}
}
```
*Lưu ý: Nếu `resultId` không tồn tại hoặc không thuộc về user hiện tại, trả về lỗi 404 Not Found.*

### 3. Lấy hoạt động học tập gần đây (Recent Activities)
**Endpoint:** `GET /v1/results/me/recent`
**Mô tả:** Lấy ra danh sách các bài thi/học tập vừa được thực hiện gần nhất của người dùng.

**Query Parameters:**
- `limit` (Integer): Số lượng trả về. Mặc định: 10.

**Response (200 OK):**
```json
[
  {
    "targetType": "EXAM",
    "targetId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "result": "PASSED",
    "score": 90.0,
    "completedAt": "2026-08-09T10:00:00Z"
  }
]
```

---

## Stage 6: Summary & Progress APIs
*Các API phục vụ truy vấn tiến độ, điểm số tổng hợp và Dashboard. Lấy dữ liệu từ bảng `learning_summaries`.*

### 4. Lấy danh sách tiến độ (Summary List)
**Endpoint:** `GET /v1/results/me/summaries`
**Mô tả:** Liệt kê tiến độ của các bài giảng/bài thi người dùng đang học.

**Query Parameters:**
- `targetType` (String): Tùy chọn (EXAM, LESSON, ...)
- `status` (String): Tùy chọn (NOT_STARTED, IN_PROGRESS, COMPLETED)
- Các tham số phân trang (`page`, `size`, `sort`). Mặc định sort: `lastActivityAt,desc`.

**Response (200 OK - Phân trang):**
```json
{
  "content": [
    {
      "targetType": "LESSON",
      "targetId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "status": "IN_PROGRESS",
      "attemptCount": 2,
      "bestScore": null,
      "latestScore": null,
      "averageScore": null,
      "progress": 55.0,
      "lastActivityAt": "2026-08-09T10:00:00Z"
    }
  ],
  "totalElements": 1
}
```

### 5. Lấy chi tiết tiến độ một đối tượng (Summary Detail)
**Endpoint:** `GET /v1/results/me/summaries/{targetType}/{targetId}`
**Mô tả:** Truy xuất tiến độ, điểm cao nhất, điểm trung bình của một đối tượng học tập.

**Response (200 OK):**
```json
{
  "targetType": "EXAM",
  "targetId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "status": "COMPLETED",
  "attemptCount": 3,
  "completionCount": 2,
  "bestScore": 95.0,
  "latestScore": 80.0,
  "averageScore": 85.5,
  "progress": 100.0,
  "totalDurationSeconds": 3600,
  "lastActivityAt": "2026-08-09T10:00:00Z",
  "completedAt": "2026-08-09T10:00:00Z",
  "summarySnapshot": {},
  "metadata": {}
}
```

### 6. Thống kê theo trạng thái (Status Statistics)
**Endpoint:** `GET /v1/results/me/summaries/status`
**Mô tả:** Đếm tổng số lượng bài đã hoàn thành, đang học, và chưa bắt đầu của người dùng.

**Response (200 OK):**
```json
{
  "completed": 15,
  "inProgress": 3,
  "notStarted": 0
}
```

### 7. Thống kê tổng hợp theo loại (Progress by Target Type)
**Endpoint:** `GET /v1/results/me/summaries/progress`
**Mô tả:** Tính tỷ lệ hoàn thành (Completion Rate) và mức tiến độ trung bình của một nhóm đối tượng cụ thể (Ví dụ: LESSON).

**Query Parameters:**
- `targetType` (Bắt buộc): Loại đối tượng.

**Response (200 OK):**
```json
{
  "targetType": "LESSON",
  "total": 10,
  "completed": 6,
  "inProgress": 2,
  "notStarted": 2,
  "completionRate": 60.0,
  "averageProgress": 75.5
}
```

### 8. Gợi ý học tập (Resume Target)
**Endpoint:** `GET /v1/results/me/summaries/resume`
**Mô tả:** Trả về đối tượng học tập dở dang gần nhất mà người dùng nên tiếp tục học (Dựa vào `lastActivityAt` và trạng thái `IN_PROGRESS`).

**Response (200 OK):**
```json
{
  "targetType": "LESSON",
  "targetId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "progress": 75.0,
  "lastActivityAt": "2026-08-09T10:00:00Z"
}
```
*Lưu ý: Nếu không có đối tượng nào đang dở dang, trả về body rỗng (Status 200 OK).*

---
## Xử lý Lỗi (Global Error Handling)
Bất kỳ ngoại lệ truy xuất tài nguyên không tồn tại hoặc bị từ chối truy cập (Ownership violation) đều trả về chuẩn lỗi sau:

```json
{
  "timestamp": "2026-08-09T10:05:00Z",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Learning result not found"
}
```
