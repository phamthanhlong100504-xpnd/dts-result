# Consumer blueprint — kết quả, EXP evidence và retry

## Transport

LearningResultConsumer đọc spring.kafka.topic.learning-results, group spring.kafka.consumer.group-id (mặc định result-service-group). @RetryableTopic attempts3, backoff1000ms*2/max5000, template learningResultRetryKafkaTemplate, DLT strategy FAIL_ON_ERROR. ValidationException và api BusinessException được exclude khỏi retry topic policy; không mô tả chúng là lỗi tạm phải thử vô hạn.

KafkaConfig producer serializer hỗ trợ cả LearningResultEvent đã decode và byte[] gốc của ErrorHandlingDeserializer để retry/DLT không lỗi cast. YAML JsonDeserializer mặc định LearningResultEvent, use.type.headers=false để producer package khác vẫn deserialize về DTO local. Topic beans learning-results và learning-results-dlt có3 partitions/replicas1. Đổi topic qua environment cần rà cả consumer/topic bean hiện hard-code; gói Git giữ cấu hình đang dùng.

LearningResultDltConsumer hiện có @DltHandler ghi log event/error; không có bảng lưu DLT mới hoặc API tự replay mọi dead letter. Không suy log DLT là result đã được ghi. MDC correlation được clear trong finally.

## Phân luồng

QUESTION_CORRECT_RECORDED/PRACTICE_EXP_SOURCE_READY đi StudyExpEventService; các event còn lại đi LearningResultService. Correct-question evidence không tạo History/Summary, không tạo Progress event. Hai event EXP dùng eventVersion1, khác result EXAM typed V2.

Result EXAM V2 yêu cầu LEARNING_RESULT_CREATED,eventId,userId,sourceType/sourceId,targetType EXAM,targetId,attemptNo>=1,PASSED/FAILED,startedAt<=completedAt,score>=0/maxScore>0/score<=maxScore,duration>=0,activityType,licenseClass,questionCount>0/correctCount hợp lệ,resultAvailable=true. OFFICIAL_EXAM ↔ EXAM_SESSION; RANDOM/PRESET ↔ PRACTICE_SESSION. displayTitle nullable<=200. Thời điểm release do producer sở hữu; SUBMITTED không được lưu thành kết quả public.

Khóa source/user trong transaction và unique source key chống concurrent replay. Enrichment V1→V2 giữ row ID/createdAt, rebuild summary; V2 lặp không thay snapshot. Không dùng attemptNo làm dedup key vì các version official có thể cùng attemptNo.

## EXP / source policy

QUESTION_CORRECT_RECORDED: source PRACTICE_QUESTION,target QUESTION,eventId=sourceId,occurredAt>=captureStartedAt; metadata bankVersion,questionId integer>0,context,captureStartedAt. Context năm loại theo source; user/license bắt buộc. ON CONFLICT(eventId) DO NOTHING rồi đối chiếu exact facts để từ chối reuse id khác nội dung.

PRACTICE_EXP_SOURCE_READY: source PRACTICE_CONFIG,target CONFIG,user=null,eventId=sourceId. Metadata bankVersion,captureStartedAt,policyRevision>=1,licenseQuestionCounts map không rỗng/count1..1000. Policy authoritative dùng scale từng hạng. Không lấy lượt học hoặc câu mở xem để bịa correctness.

[Public profile consumer](public_profiles.md) là factory/group riêng với raw JSON; nó không thay decoder hoặc retry template learning-results. Nguồn: [LearningResultConsumer](../../src/main/java/com/dts/result/infrastructure/kafka/LearningResultConsumer.java), [LearningResultService](../../src/main/java/com/dts/result/application/service/LearningResultService.java), [StudyExpEventService](../../src/main/java/com/dts/result/application/service/StudyExpEventService.java).
