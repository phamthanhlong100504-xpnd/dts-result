# Summary Projection Engine (Stage 4)

## Tổng quan
Tài liệu này định nghĩa thuật toán Projection và các quy tắc cập nhật (Update Rules) cho bảng `learning_summaries`. Nhiệm vụ của Projection Engine là duy trì bản tóm tắt trạng thái học tập (tiến độ, điểm số, số lần làm bài...) của một người dùng đối với một đối tượng học tập (bài thi, chương học, khóa học) theo thời gian thực.

## Nguyên tắc Projection
- **Bảng `learning_results`**: Lưu trữ lịch sử (chi tiết từng lần thực hiện).
- **Bảng `learning_summaries`**: Lưu trữ trạng thái hiện tại (tổng hợp mới nhất).
Mỗi khi có một bản ghi mới trong `learning_results`, bảng `learning_summaries` tương ứng (xác định bởi `user_id`, `target_type`, `target_id`) sẽ được tạo mới hoặc cập nhật (Upsert).

## Quy tắc Upsert (Update Rules)

Trường hợp **Chưa tồn tại Summary (Tạo mới):**
- `attempt_count`: Khởi tạo bằng `1`.
- `completion_count`: `1` nếu result là `PASSED`, `COMPLETED` hoặc `SUBMITTED`, ngược lại là `0`.
- `best_score`, `latest_score`, `average_score`: Gán bằng điểm `score` của event.
- `progress`: Gán bằng `progress` của event.
- `status`: Được tính toán dựa trên `progress` và `result`.
- `total_duration_seconds`: Khởi tạo bằng `duration`.
- `last_activity_at`: `completedAt` của event.

Trường hợp **Đã tồn tại Summary (Cập nhật):**
- `attempt_count`: `old_count + 1`.
- `completion_count`: Tăng thêm `1` nếu event result thuộc nhóm thành công.
- `best_score`: `MAX(old_best_score, new_score)`.
- `latest_score`: `new_score`.
- `average_score`: Tính trung bình lũy tiến: `((old_average * old_attempts) + new_score) / new_attempts`. Tránh việc phải truy vấn lại toàn bộ lịch sử.
- `progress`: `MAX(old_progress, new_progress)`. Progress không bao giờ giảm.
- `status`: Dựa theo progress lớn nhất hiện tại.
    - `0`: NOT_STARTED
    - `> 0` và `< 100`: IN_PROGRESS
    - `>= 100` hoặc eventResult trả về PASSED/COMPLETED: COMPLETED
- `total_duration_seconds`: `old_duration + new_duration`.
- `last_result_id`: Trỏ tới `ID` của bản ghi `learning_results` vừa được thêm mới.
- `last_activity_at`: Cập nhật thời điểm hoàn thành mới nhất.
- `completed_at`: Chỉ set nếu `status` chuyển sang `COMPLETED` và trước đó giá trị này chưa có (null).

## Bảo toàn Concurrency
Để đảm bảo an toàn khi xử lý đồng thời, toàn bộ thuật toán Projection chạy bên trong một **Transaction** chung với quá trình Insert dữ liệu vào `learning_results`. Việc tìm kiếm để cập nhật trong PostgreSQL sẽ sử dụng khóa dòng (Row-level Locking) hoặc cơ chế Upsert chuẩn của Database nhằm tránh **Race Condition** khi người dùng spam gửi kết quả nhiều lần cùng lúc.
