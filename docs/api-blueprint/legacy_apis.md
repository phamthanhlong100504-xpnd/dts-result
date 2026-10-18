# Các API nền và phạm vi tương thích

## Read API dưới /v1/results/me

Overview: GET /v1/results/me/overview tổng hợp learning_summaries. Summary/status/progress/resume xem [History & Summary](history_summary_api_blueprint.md). JWT subject quyết định user.

Statistics: GET /v1/results/me/statistics?from=...&to=...&interval=DAY, mặc định30 ngày, code giới hạn span365 ngày. Source nhóm daily UTC, dùng raw score; interval hiện không thay thuật toán grouping. Không có normalization theo Practice scale, không thay được analytics30D/7D. IllegalArgumentException giới hạn ngày không có handler400 riêng, không hứa mọi lỗi legacy được map giống API mới.

## Learning-log / tracking trực tiếp

| Method/path | Contract nguồn |
| --- | --- |
| POST /api/v1/result-service/learning-logs | X-Tenant-Id,X-User-Id UUID; RecordLearningLogRequest;201 LearningLogResponse |
| GET cùng path | X-Tenant-Id, query userId/page default1/size20; PageResponse items |
| POST /api/v1/result-service/tracking-events | X-Tenant-Id,X-User-Id UUID; RecordTrackingEventRequest;201 TrackingEventResponse |
| GET cùng path | X-Tenant-Id, query userId/page1/size20; PageResponse items |

Đây là header/query contract legacy, không lấy user từ principal như /v1/results/me. SecurityConfig hiện permitAll ngoài nhóm protected, không mô tả các route này là JWT-scoped an toàn tương đương. Gateway result proxy chỉ cho /v1 nên không public-proxy các path legacy này. Giữ source hiện hành theo ràng buộc không đổi logic.

LearningLogService lưu tenant/user/content/session/time/device/metadata; không chấm điểm, không cấp EXP. TrackingEventService dedup theo tenantId+idempotencyKey rồi lưu entity/version/language/context/payload; không có minh chứng nó là canonical Practice correctness. List page1-based, service chuyển max(0,page-1), khác History0-based.

## Entity chưa có endpoint

Repo còn UserContentAttempt/Result,UserCriteriaResult,bookmark/note/reaction entity/form/repository nhưng không có controller tương ứng trong snapshot. Không phát minh REST route dựa tên class, không xóa chúng vì chưa dùng trong phần mới.

Canonical migration V001–V004 chỉ tạo learning_results/learning_summaries và projection4 bảng, không tạo8 bảng JPA legacy khác. API legacy cần schema cũ của môi trường; không đặt Hibernate update để tự phát sinh bảng trong bước Git. [Migration catalog](../db/MIGRATION_CATALOG.md).
