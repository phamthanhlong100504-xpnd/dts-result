# Bộ Mẫu Data Test Postman — Dịch Vụ dts-result (Port 8083)

Bộ dữ liệu thử nghiệm chuẩn cho 15 APIs của dịch vụ `dts-result` (`result-service`) để nhập vào Postman.

- **Base URL (Trực tiếp)**: `http://localhost:8083`
- **Base URL (Qua Gateway)**: `http://localhost:8080` (nếu chạy dts-gateway)

---

## 📌 Headers Bắt Buộc Chung Cho Mọi Yêu Cầu

Tất cả các API đều yêu cầu Headers xác thực Tenant và User:

| Header Key | Header Value | Mô tả |
|---|---|---|
| `-Type` | `application/json` | Bắt buộc đối với phương thức POST/PUT |
| `X-Tenant-Id` | `0190ce1a-0000-7000-8000-000000000000` | ID tenant khớp với dữ liệu seed |
| `X-User-Id` | `0190ce1a-0000-7000-8000-000000000099` | ID learner khớp với dữ liệu seed |

---

## 1. Nộp bài làm & chấm điểm (STT 1)

- **Method**: `POST`
- **URL**: `http://localhost:8083/api/v1/result-service/content-attempts`
- **Body (JSON)**:

```json
{
  "nodeId": "0190ce1a-0000-7000-8000-000000000001",
  "contentId": "0190ce1a-2000-7000-8000-000000000001",
  "contentType": "EXAM",
  "startedAt": "2026-08-03T10:00:00Z",
  "submittedAt": "2026-08-03T11:00:00Z",
  "durationSec": 3600,
  "timeTakenSec": 3000,
  "deviceKind": "desktop",
  "sessionId": "0190ce1a-7777-7000-8000-000000000001",
  "contentVersionId": "0190ce1a-3000-7000-8000-000000000001",
  "sourceService": "lms-exam",
  "sourceRef": "exam-session-123",
  "answers": [
    {
      "questionId": "0190ce1a-5000-7000-8000-000000000001",
      "questionVersionId": "0190ce1a-5000-7000-8000-000000000002",
      "answer": {
        "optionId": "0190ce1a-5000-7000-8000-000000000003"
      },
      "isCorrect": true,
      "score": 10.0,
      "maxScore": 10.0
    }
  ],
  "proctoringData": {
    "tabSwitchCount": 0,
    "webcamEnabled": true
  }
}
```

---

## 2. Lấy chi tiết lượt nộp bài (STT 2)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/content-attempts/0190ce1a-4000-7000-8000-000000000001`
- **Body**: *None*

---

## 3. Lấy lịch sử nộp bài (STT 3)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/content-attempts?userId=0190ce1a-0000-7000-8000-000000000099&page=1&size=20`
- **Body**: *None*

---

## 4. Lấy cây tiến độ học tập (STT 4)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/users/0190ce1a-0000-7000-8000-000000000099/content-results`
- **Body**: *None*

---

## 5. Lấy chi tiết node tiến độ (STT 5)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/content-results/0190ce1a-0000-7000-8000-000000000001`
- **Body**: *None*

---

## 6. Tính toán lại tiến độ node (STT 6)

- **Method**: `PUT`
- **URL**: `http://localhost:8083/api/v1/result-service/content-results/0190ce1a-0000-7000-8000-000000000001/recalculate`
- **Body**: `{}` (rỗng hoặc không chọn Body)

---

## 7. Ghi nhận sự kiện tracking (STT 7)

- **Method**: `POST`
- **URL**: `http://localhost:8083/api/v1/result-service/tracking-events`
- **Body (JSON)**:

```json
{
  "eventType": "lesson.started",
  "entityKind": "lesson",
  "entityId": "0190ce1a-2000-7000-8000-000000000002",
  "versionId": "0190ce1a-3000-7000-8000-000000000002",
  "versionNo": 1,
  "language": "vi-VN",
  "occurredAt": "2026-08-03T11:00:00Z",
  "source": "client_web",
  "idempotencyKey": "idempotency-key-test-postman-001",
  "payload": {
    "device": "desktop",
    "os": "Windows"
  },
  "context": {
    "ip": "127.0.0.1"
  }
}
```

---

## 8. Truy vấn sự kiện tracking (STT 8)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/tracking-events?userId=0190ce1a-0000-7000-8000-000000000099&page=1&size=20`
- **Body**: *None*

---

## 9. Ghi nhận nhật ký học tập (STT 9)

- **Method**: `POST`
- **URL**: `http://localhost:8083/api/v1/result-service/learning-logs`
- **Body (JSON)**:

```json
{
  "nodeId": "0190ce1a-0000-7000-8000-000000000002",
  "contentId": "0190ce1a-2000-7000-8000-000000000002",
  "contentType": "VIDEO",
  "sessionKind": "MEDIA_VIEW",
  "startedAt": "2026-08-03T10:00:00Z",
  "endedAt": "2026-08-03T10:30:00Z",
  "durationSec": 1800,
  "mediaPositionSec": 1800,
  "deviceKind": "desktop",
  "metadata": {
    "playbackSpeed": 1.0,
    "quality": "1080p"
  }
}
```

---

## 10. Lấy danh sách nhật ký học (STT 10)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/learning-logs?userId=0190ce1a-0000-7000-8000-000000000099`
- **Body**: *None*

---

## 11. Lấy kết quả tiêu chí user (STT 11)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/users/0190ce1a-0000-7000-8000-000000000099/criteria-results`
- **Body**: *None*

---

## 12. Đánh giá tiêu chí hoàn thành (STT 12)

- **Method**: `POST`
- **URL**: `http://localhost:8083/api/v1/result-service/users/0190ce1a-0000-7000-8000-000000000099/criteria-results/evaluate`
- **Body (JSON)**:

```json
{
  "nodeId": "0190ce1a-0000-7000-8000-000000000001",
  "triggerSource": "PROGRESS_UPDATE"
}
```

---

## 13. Toggle bookmark nội dung (STT 13)

- **Method**: `POST`
- **URL**: `http://localhost:8083/api/v1/result-service/bookmarks/toggle`
- **Body (JSON)**:

```json
{
  "contentId": "0190ce1a-2000-7000-8000-000000000002",
  "contentType": "LESSON",
  "nodeId": "0190ce1a-0000-7000-8000-000000000002",
  "note": "Bài học hay cần xem lại trước khi thi"
}
```

---

## 14. Tạo / Cập nhật ghi chú (STT 14)

- **Method**: `POST`
- **URL**: `http://localhost:8083/api/v1/result-service/notes`
- **Body (JSON)**:

```json
{
  "contentId": "0190ce1a-2000-7000-8000-000000000002",
  "nodeId": "0190ce1a-0000-7000-8000-000000000002",
  "mediaTimestampSec": 450,
  "noteText": "Cần chú ý định lý và công thức tại phút 7:30"
}
```

---

## 15. Thả reaction nội dung (STT 15)

- **Method**: `POST`
- **URL**: `http://localhost:8083/api/v1/result-service/reactions`
- **Body (JSON)**:

```json
{
  "contentId": "0190ce1a-2000-7000-8000-000000000002",
  "contentType": "LESSON",
  "reactionType": "LIKE"
}
```
