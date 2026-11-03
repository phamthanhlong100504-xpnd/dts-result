# Bộ Mẫu Data Test Postman — Dịch Vụ dts-result (Schema Mới)

Bộ dữ liệu thử nghiệm chuẩn cho các APIs của dịch vụ `dts-result` (`result-service`) để nhập vào Postman.

- **Base URL (Trực tiếp)**: `http://localhost:8083`
- **Base URL (Qua Gateway)**: `http://localhost:8080` (nếu chạy dts-gateway)

---

## 📌 Headers Bắt Buộc Chung Cho Mọi Yêu Cầu

| Header Key | Header Value | Mô tả |
|---|---|---|
| `Content-Type` | `application/json` | Bắt buộc đối với phương thức POST/PUT |
| `X-User-Id` | `0190ce1a-0000-7000-8000-000000000099` | ID learner khớp với dữ liệu seed |

---

## 1. Ghi nhận kết quả học tập / bài thi (POST)

- **Method**: `POST`
- **URL**: `http://localhost:8083/api/v1/result-service/learning-results`
- **Body (JSON)**:

```json
{
  "sourceId": "0190ce1a-7777-7000-8000-000000000002",
  "targetId": "0190ce1a-2000-7000-8000-000000000001",
  "sourceType": "EXAM_SESSION",
  "targetType": "EXAM",
  "score": 95.00,
  "maxScore": 100.00,
  "progress": 100.00,
  "durationSeconds": 3000,
  "result": "PASSED",
  "startedAt": "2026-08-05T10:00:00Z",
  "completedAt": "2026-08-05T10:50:00Z",
  "resultSnapshot": {
    "correctQuestions": 19,
    "wrongQuestions": 1,
    "passingScore": 80
  },
  "metadata": {
    "tabSwitchCount": 0
  }
}
```

---

## 2. Lấy chi tiết kết quả lượt thực hiện (GET)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/learning-results/0190ce1a-4000-7000-8000-000000000001`
- **Body**: *None*

---

## 3. Truy vấn danh sách kết quả học tập (GET)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/learning-results?page=1&size=20`
- **Body**: *None*

---

## 4. Lấy danh sách tiến độ tổng hợp của user (GET)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/users/0190ce1a-0000-7000-8000-000000000099/learning-summaries`
- **Body**: *None*

---

## 5. Lấy chi tiết tiến độ tổng hợp (GET)

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/v1/result-service/learning-summaries/0190ce1a-0000-7000-8000-000000000001`
- **Body**: *None*

---

## 6. Tính toán lại tiến độ tổng hợp (PUT)

- **Method**: `PUT`
- **URL**: `http://localhost:8083/api/v1/result-service/learning-summaries/0190ce1a-0000-7000-8000-000000000001/recalculate`
- **Body**: *None*
