# Kế Hoạch Thiết Kế API Blueprint — Dịch Vụ dts-result (Result & Tracking Service) (Đã phê duyệt)

- **Thời gian phê duyệt**: 03-08-2026 10:01 (local time)
- **Mục tiêu**: Chuẩn hóa URL theo quy tắc `/api/{version}/{service}/{object}` và sinh danh sách các tài liệu API Blueprint nhỏ, độc lập cho các nghiệp vụ Quản lý Kết quả, Tiến độ học tập, Lịch sử nộp bài, Tracking Events, Nhật ký học tập, Tiêu chí hoàn thành và Tương tác người dùng trong dịch vụ `dts-result` (`result-service`).

---

## 1. Cập Nhật Quy Tắc (`rules/global/02_api.md`)

- Cập nhật mục **Versioning** và **Resource Naming** trong file `rules/global/02_api.md` quy định định dạng đường dẫn bắt buộc:
  ```
  /api/{version}/{service}/{object}
  ```

---

## 2. Danh Sách Các API Blueprint Độc Lập Sẽ Sinh

Các file tài liệu được tách nhỏ từng nghiệp vụ và lưu tại `docs/api-blueprint/`:

| STT | Nghiệp vụ | HTTP Method | REST Endpoint | File Blueprint tương ứng |
|---|---|---|---|---|
| 1 | **Nộp bài làm & chấm điểm** | `POST` | `/api/v1/result-service/content-attempts` | `docs/api-blueprint/submit_content_attempt.md` |
| 2 | **Lấy chi tiết lượt nộp bài** | `GET` | `/api/v1/result-service/content-attempts/{id}` | `docs/api-blueprint/get_content_attempt_detail.md` |
| 3 | **Lấy lịch sử nộp bài** | `GET` | `/api/v1/result-service/content-attempts` | `docs/api-blueprint/list_content_attempts.md` |
| 4 | **Lấy cây tiến độ học tập** | `GET` | `/api/v1/result-service/users/{userId}/content-results` | `docs/api-blueprint/get_user_progress_tree.md` |
| 5 | **Lấy chi tiết node tiến độ** | `GET` | `/api/v1/result-service/content-results/{id}` | `docs/api-blueprint/get_content_result_detail.md` |
| 6 | **Tính toán lại tiến độ node** | `PUT` | `/api/v1/result-service/content-results/{id}/recalculate` | `docs/api-blueprint/recalculate_content_result.md` |
| 7 | **Ghi nhận sự kiện tracking** | `POST` | `/api/v1/result-service/tracking-events` | `docs/api-blueprint/record_tracking_event.md` |
| 8 | **Truy vấn sự kiện tracking** | `GET` | `/api/v1/result-service/tracking-events` | `docs/api-blueprint/list_tracking_events.md` |
| 9 | **Ghi nhận nhật ký học tập** | `POST` | `/api/v1/result-service/learning-logs` | `docs/api-blueprint/record_learning_log.md` |
| 10 | **Lấy danh sách nhật ký học** | `GET` | `/api/v1/result-service/learning-logs` | `docs/api-blueprint/list_learning_logs.md` |
| 11 | **Lấy kết quả tiêu chí user** | `GET` | `/api/v1/result-service/users/{userId}/criteria-results` | `docs/api-blueprint/get_user_criteria_results.md` |
| 12 | **Đánh giá tiêu chí hoàn thành** | `POST` | `/api/v1/result-service/users/{userId}/criteria-results/evaluate` | `docs/api-blueprint/evaluate_criteria_results.md` |
| 13 | **Toggle bookmark nội dung** | `POST` | `/api/v1/result-service/bookmarks/toggle` | `docs/api-blueprint/toggle_content_bookmark.md` |
| 14 | **Tạo / Cập nhật ghi chú** | `POST` | `/api/v1/result-service/notes` | `docs/api-blueprint/save_lesson_note.md` |
| 15 | **Thả reaction nội dung** | `POST` | `/api/v1/result-service/reactions` | `docs/api-blueprint/save_content_reaction.md` |

---

## 3. Tiêu Chuẩn Cấu Trúc Blueprint (Tuân thủ `api-blueprint-generator.md`)

Mỗi file trong các file trên phải có đủ 5 phần bắt buộc:
1. **Part 0 — Classification & Identity**: API Name, Type, Module, Feature, Description, Related Tables, Related Services.
2. **Part 1 — API Contract**: URL, Method, Request Table, Response Table, Error Codes Table.
3. **Part 2 — Processing Specification**: Các bước xử lý đánh số tuần tự (Controller -> Service -> Repository -> External -> Validation).
4. **Part 3 — Data Interaction**: Thao tác SQL trừu tượng (SELECT, INSERT, UPDATE, soft delete UPDATE).
5. **Part 4 — Operational Notes**: Idempotency, Audit Logging, Tracing (`traceId`), Tenant Isolation, Cache Eviction/Query.
