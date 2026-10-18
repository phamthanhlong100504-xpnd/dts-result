# Learning result processing hiện hành

LearningResultConsumer dispatch EXP evidence sang StudyExpEventService, các result envelope khác sang LearningResultService. [Consumer blueprint](api-blueprint/consumer_blueprint.md) mô tả transport/retry/DTO; [RESULT_FEATURES](RESULT_FEATURES.md) giải thích nghiệp vụ.

## Transaction và khóa nguồn

Validate user/source/target/result; EXAM chỉ completed released PASSED/FAILED với source attemptNo. ResultWriteLock advisory khóa source trước, user sau trong cùng transaction. Unique source identity bảo vệ cuối ở DB, không dựa existence check đơn lẻ trước insert.

Cùng sourceType/sourceId đã có thì return khi cùng user, reject nếu user khác. V2 có thể enrich V1, giữ id/createdAt và rebuild EXAM summary cho target mới/cũ. Không ghi đè một V2 đã lưu bằng snapshot retry khác. Non-exam thiếu attemptNo có nhánh legacy count+1; exam luôn giữ số lượt nguồn, không dedup theo attemptNo.

## Kết quả và công bố

V2 lưu field typed activity/license/title/counts/result/review. Source grade/raw maxScore được giữ, Result không chấm lại theo bank hiện tại. SUBMITTED không được lưu như public EXAM result. Producer Practice/Examination có trách nhiệm gate release trước publish. Public History còn lọc completed/resultAvailable/PASSED/FAILED và owner ở mỗi lần đọc.

EXAM dùng ExamHistoryProjection rebuild persisted released facts, non-exam dùng LearningSummaryService legacy. Evidence câu đúng/policy không đi đường này và không tạo History/Progress events. Không có cộng dồn EXP trực tiếp trong LearningResultService; leaderboard query tính từ facts theo policy.

## Retry/DLT

learningResultRetryKafkaTemplate hỗ trợ event object và raw bytes. RetryableTopic attempts3, backoff1000ms multiplier2/max5000; validation/business exceptions không retry theo annotation. DLT handler hiện ghi log, không có database dead-letter mới. Consumer profile có factory/group riêng, không đổi decoder main.

Schema/version/hash nằm [Migration catalog](db/MIGRATION_CATALOG.md). Đợt Git giữ nguyên service/consumer/SQL local, không gọi Kafka/replay hoặc sửa các record lịch sử.
