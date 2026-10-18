# API Blueprint: History và summary hiện hành

Bearer, principal UUID từ JWT. Path trực tiếp Result; Gateway thêm /api/result trước /v1.

## GET /v1/results/me/history

page0-based mặc định0, size20/1..100; page0..Integer.MAX_VALUE. Sort mặc định completedAt desc, whitelist completedAt/startedAt/score/id; không ignoreCase, backend thêm id desc nếu chưa có. Response Spring Page<HistoryItemResponse>, danh sách **content**.

| Query | Giá trị |
| --- | --- |
| targetType | EXAM,PRACTICE_SET,CHAPTER,LESSON,LEARNING_PROGRAM |
| targetId | UUID |
| result | PASSED,FAILED,COMPLETED,ABANDONED,EXPIRED,SUBMITTED; EXAM chỉ PASSED/FAILED |
| activityType | RANDOM_EXAM,PRESET_EXAM,OFFICIAL_EXAM |
| licenseClass | Hạng hiện hành hoặc A2,B2,E,F legacy được source chấp nhận |
| from/to | Instant ISO8601, from<=to; completedAt>=from và <=to |

Không có query range=7/30/90 trong controller; frontend chuyển lựa chọn ngày thành from/to. EXAM chưa completed/PASSED/FAILED/resultAvailable=true luôn bị lọc. Item có id(resultId),sourceType/sourceId,targetType/targetId,attemptNo,result,score/maxScore/progress,durationSeconds,startedAt/completedAt cùng activityType/displayTitle/licenseClass/correctCount/questionCount/resultAvailable/reviewAvailable/reasonCode/sourceResult.

## GET /v1/results/me/history/{resultId}

200 HistoryDetailResponse; owner khác hoặc exam chưa release404. resultId là learning_results.id, không phải exam/sessionId. EXAM resultSnapshot và metadata chỉ whitelist scalar safe, không trả raw answer keys/nested snapshot. Non-exam giữ legacy snapshot/metadata như source hiện có.

sourceResult={sourceType,sourceId,resultPath}, resultPath /practice/result/{id} hoặc /examination/result/{id}; frontend dùng điều hướng web. Source vẫn kiểm quyền/review khi người dùng mở. reviewAvailable null => SOURCE_POLICY_REQUIRED; false => REVIEW_NOT_ALLOWED; true => reasonCode null. Title có thể null cho lịch sử cũ, không lấy tên hiện tại thay lịch sử.

## GET /v1/results/me/recent

limit default10, hợp lệ1..100 nhưng repository chỉ lấy top10 trước lọc unreleased EXAM và cắt limit. Có thể trả ít hơn yêu cầu; List<RecentActivityResponse>, không phải Page. Item targetType,targetId,sourceId,result,score,completedAt; không dùng thay analytics recentResults có normalized score.

## Summary APIs

| Method/path | Response |
| --- | --- |
| GET /v1/results/me/summaries | Page<SummaryItemResponse>, query targetType/status/pageable, size20/sort lastActivityAt desc |
| GET /v1/results/me/summaries/{targetType}/{targetId} | SummaryDetailResponse theo user/target, thiếu404 |
| GET /v1/results/me/summaries/status | completed,inProgress,notStarted |
| GET /v1/results/me/summaries/progress?targetType=... | Tổng/status, completionRate và averageProgress |
| GET /v1/results/me/summaries/resume | ResumeResponse hoặc200 body rỗng khi chưa có target |

Summary lấy learning_summaries, EXAM được rebuild từ nguồn released; không áp journal loại projection hay EXPcap. best/latest/average dùng source score raw, có thể khác đơn vị giữa các target. Không thay analytics averageScore bằng Summary averageScore. Xem [summary-projection](../summary-projection.md).

Nguồn: [HistoryController](../../src/main/java/com/dts/result/api/controller/HistoryController.java), [HistoryService](../../src/main/java/com/dts/result/application/service/HistoryService.java), [SummaryController](../../src/main/java/com/dts/result/api/controller/SummaryController.java).
