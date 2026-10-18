# Chức năng và nghiệp vụ DTS Result

## 1. Phạm vi và cấu trúc

Result sở hữu kết quả đã nhận, summary, evidence phục vụ EXP, journal điều chỉnh projection và cache profile công khai. Practice/Examination sở hữu câu trả lời, grade và release; Identity sở hữu tên, tài khoản và JWT. Result không chấm lại bài từ câu hỏi hiện tại và không có nguồn canonical để sửa ba TIMEOUT bất thường đã được giữ nguyên.

| Nhóm source | Vai trò |
| --- | --- |
| api/controller + form/response | History, summary, leaderboard, analytics và API legacy |
| LearningResultService + ResultWriteLock | Validate, dedup source và transaction lock |
| ExamHistoryProjection | Rebuild summary EXAM từ facts đã lưu |
| HistoryService | Bộ lọc, owner/release, DTO safe và source navigation |
| StudyExpEventService | Nhận evidence câu đúng và policy Practice |
| PublicProfileProjectionService | Cache tên từ user-events theo thứ tự nguồn |
| PerformanceProjectionRepository | SQL một snapshot cho leaderboard/analytics |
| config + infrastructure/kafka | Retry serializer, consumer learning-results và profile riêng |
| docs/db + tools | Canonical V001–V004, verify/hash, maintenance tường minh |

## 2. Nhận kết quả và chống trùng

LearningResultConsumer nhận envelope trên learning-results. Hai eventType QUESTION_CORRECT_RECORDED/PRACTICE_EXP_SOURCE_READY được chuyển sang StudyExpEventService; phần còn lại vào LearningResultService. Retry nguồn không cộng dồn history lần nữa.

LearningResultService yêu cầu user/source/target/result, progress hữu hạn0..100 nếu có. EXAM phải PASSED/FAILED, startedAt/completedAt có thứ tự, attemptNo dương và sourceType PRACTICE_SESSION/EXAM_SESSION. SUBMITTED chưa release bị từ chối. V2 thêm eventType/eventId, resultAvailable=true, activity/license/count/grade/duration đúng shape; OFFICIAL_EXAM đi với EXAM_SESSION, RANDOM/PRESET đi với PRACTICE_SESSION.

Khóa advisory theo source trước, theo user sau trong transaction, thêm unique source index bảo vệ DB. Dedup key là sourceType/sourceId, không phải eventId hoặc attemptNo. Source của user khác bị từ chối. V2 đến sau có thể enrich record V1, giữ id/createdAt, rebuild target mới và target cũ khi đổi. V2 đã lưu không bị bản retry V2 khác ghi đè. Non-exam legacy thiếu attemptNo dùng count+1 theo user/target; không gán lại số lượt cho exam theo số row Result.

## 3. Lịch sử và summary

History list luôn theo JWT subject; EXAM chỉ xuất hiện khi đã hoàn tất/PASSED hoặc FAILED/resultAvailable=true, kể cả không gửi targetType. Sort có whitelist, thêm id desc để phân trang ổn định; không cho sort metadata tùy ý. Detail người khác hoặc exam chưa release trả404.

V2 giữ activityType,displayTitle,licenseClass,correctCount,questionCount và result/review flags. Title lấy snapshot nguồn, có thể null với lịch sử cũ; không dùng tên bài hiện tại để viết lại history. sourceResult.resultPath là đường điều hướng web đến Practice/Examination theo sourceId; resultId dùng detail Result, không thay bằng sourceId.

Detail EXAM chỉ trả whitelist scalar snapshot/metadata, không trả answer key hoặc object nguồn tùy ý. Review vẫn phải theo quyền source ở lần request sau. Recent legacy trả tối đa10 row repository trước lọc rồi áp limit1..100; có thể ít hơn limit, không phải pagination mới.

ExamHistoryProjection tính lại từ completed released PASSED/FAILED facts: attempt count, pass completion count, max/avg/latest source score, tổng duration capped int, first pass. Last row theo completedAt desc/id desc, có pass thì status COMPLETED, chưa pass IN_PROGRESS, progress100 cho các attempt đã hoàn tất. Summary không phải điểm normalized của Analytics và không áp EXP caps/exclusion journal của Screen14/16.

Non-exam dùng LearningSummaryService legacy: tăng attempt/completion, cập nhật điểm nếu có, progress tối đa, tổng thời gian và status theo mapper. Null score không tự biến latest/best thành0. Không dùng summary legacy thay công thức accuracy của analytics.

## 4. EXP câu hỏi và policy

QUESTION_CORRECT_RECORDED ghi study_question_facts, source PRACTICE_QUESTION, target QUESTION, eventId=sourceId, user/license/bank/question/context/time/capture hợp lệ. Event lặp cùng id phải khớp evidence hoàn toàn; khác nội dung cùng id bị từ chối. Các context CHAPTER_LEARNING/CHAPTER_REVIEW/CRITICAL_PRACTICE/WRONG_REVIEW/PRACTICE_MODE dùng chung nguồn EXP.

PRACTICE_EXP_SOURCE_READY ghi policy_revision, capture_started_at, policy_observed_at,bank_version,question_counts theo hạng. Source PRACTICE_CONFIG/target CONFIG, user=null. Policy mới nhất theo capture→revision→observed→eventId tạo source_exam_scales. Không suy correctness từ Progress cũ, không tạo exam history/summary từ hai loại evidence này.

## 5. EXP bài thi và ranking

Chỉ typed V2 released EXAM facts hợp lệ, không bị projection exclusion, mới vào screen14_exam_facts. dts_exp_credits tính lại credits theo scope, không có tổng cộng dồn phải tăng lại khi replay.

| Hoạt động | Điểm | Quy tắc lựa chọn |
| --- | --- | --- |
| OFFICIAL_EXAM | correctCount*10 +200 nếu PASSED | Hai attempt sớm nhất của user/parent exam trên toàn lịch sử đủ điều kiện trước cutoff, rồi lọc kỳ |
| RANDOM_EXAM | correctCount*2 +30 nếu PASSED | Năm completion đầu mỗi user/hạng/ngày Việt Nam |
| PRESET_EXAM | correctCount*2 +30 nếu PASSED | Một attempt có EXP cao nhất mỗi user/hạng/preset trong scope |
| Câu học đúng |1 | Một lần cho user/hạng/bank/question trong scope, chung mọi context |

Ví dụ preset21 đúng/PASSED=72,22 đúng/FAILED vì critical=44;72 được chọn. Mọi attempt vẫn tồn tại History/Analytics dù không được credit vì cap. Official credit cap không cho phép hoặc cấm retry; quyền thi nằm Examination.

Tuần bắt đầu thứ Hai00:00, tháng ngày100:00 Asia/Ho_Chi_Minh. Window credit [from,effectiveEnd), effectiveEnd không vượt asOf. ALL_TIME from/to null. Result hoàn tất lúc nào thuộc kỳ đó, không chuyển sang ngày Kafka nhận khi release trễ.

Ranking EXP desc, lastCreditedAt asc, user UUID asc; lastCreditedAt là MAX timestamp của các credit đóng góp. rank toàn cục, không reset trang. currentUser độc lập items; không EXP dương thì score0/rank null. Profile chưa có vẫn có thể tham gia với PENDING; profile deleted bị loại khỏi ranking.

## 6. Profile công khai

Consumer user-events dùng group riêng và raw string factory, không đổi decoder learning-results. Ghi userId,displayName,deleted,source timestamp/order/partition/offset; không cache email/phone/token. USER_CREATED/UPDATED/DELETED thứ tự1/2/3; tuple nguồn lớn hơn mới thay cache. Delete đặt name null/deleted=true, không xóa History.

AVAILABLE có tên; PENDING chưa có row; UNAVAILABLE đã biết thiếu tên hoặc deleted ở currentUser. avatarUrl luôn null theo nguồn hiện tại. Không tạo tên giả hoặc API frontend tự ghi profile. Bootstrap user cũ là data maintenance có bằng chứng admin và guard, không chạy tự động.

## 7. Analytics và đơn vị

Scope user/hạng/kỳ/release/exclusion thống nhất giữa summary, chart và recent. 7D/30D là rolling7/30*24 giờ, ALL không lower bound; daily grouping theo Việt Nam. Scale lấy question_count hiện tại của Practice policy, không trộn raw Practice phần trăm với raw official trọng số.

averageScore=AVG(correctCount/questionCount*scale), làm tròn2 số; passRate=PASSED/attempts*100; accuracyRate=SUM(correctCount)/SUM(questionCount)*100. Tính từ toàn bộ eligible attempts, không cap EXP. maxScore=scale, scoreBasis NORMALIZED_CORRECT_COUNT. Giữ PASSED/FAILED từ source, không suy lại pass bằng normalized score.

Chart DAILY_AVERAGE chỉ ngày có dữ liệu, tối đa3660 ngày quan sát gần nhất; summary vẫn toàn kỳ, có cờ truncated/totalDays/limit. Recent5 mới nhất theo completedAt desc/resultId desc, giữ sourceScore/sourceMaxScore bên cạnh điểm normalized. Thiếu policy => SOURCE_POLICY_PENDING, normalized score/maxScore null, counts/passRate/accuracy vẫn trả; known-policy rỗng trả READY/zero/arrays rỗng.

## 8. API nền và giới hạn schema

Summary/overview/statistics legacy dùng dữ liệu raw và scope khác analytics mới. Statistics nhóm ngày UTC, interval hiện không đổi thuật toán ngày; không gọi nó là chart normalized Việt Nam. Learning-log/tracking-events dùng tenant/user header/query và page1-based, không cùng security contract /v1/results/me/**. Bookmark/note/reaction/attempt có entity/form nhưng không có controller tương ứng trong snapshot; không hứa frontend route chưa được khai báo.

Canonical migrations tạo learning_results/learning_summaries và4 bảng projection, không tạo8 entity table legacy khác. Giữ ddl-auto=none; các endpoint legacy cần schema cũ đúng môi trường. Gói Git giữ source nền, không bổ sung chức năng/DDL mới để lấp những phần chưa nằm trong scope đã test web.

## 9. Migration và maintenance

Schema history dts_result_schema_history; V001 baseline hai bảng, V002integrity, V003history typed, V004EXP/profile/analytics. Data bootstrap và adjustment journal là file chạy tường minh riêng; không gọi từ boot/client/student.

Adjustment append journal loại hoặc khôi phục source đã xác minh khỏi EXP/analytics, không xóa History/source row. Bootstrap chỉ insert missing minimal profile có xác nhận Identity admin; không bịa Kafka receipt. Đợt Git chỉ đồng bộ hash manifest của maintenance script đã sửa từ trước, giữ toàn bộ SQL/version history; không thực thi chúng.
