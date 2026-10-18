# Summary projection — EXAM rebuild và non-exam legacy

## EXAM

ExamHistoryProjection.rebuild yêu cầu transaction hiện có và caller giữ ResultWriteLock. Đọc learning_results theo user/targetType EXAM/targetId, completedAt không null, result PASSED/FAILED, resultAvailable=true. Rebuild từ facts để late event/enrichment không tăng attempt lần nữa.

| Field | Nguồn |
| --- | --- |
| attemptCount | COUNT eligible facts |
| completionCount | COUNT PASSED |
| bestScore/averageScore | MAX/ROUND AVG raw source score |
| latestScore/lastResultId/lastActivityAt | Row mới nhất completedAt desc/id desc |
| progress |100 cho bộ facts đã hoàn tất |
| totalDurationSeconds | SUM duration, capped2147483647 |
| status | COMPLETED nếu có PASSED, còn lại IN_PROGRESS |
| completedAt | MIN completedAt của PASSED |
| summarySnapshot/metadata | Snapshot/metadata row latest |

Upsert khóa user/target. Khi enrichment đổi target từ legacy versionId sang parent examId, rebuild target cũ và chỉ xóa projection EXAM rỗng nếu không còn result source bất kỳ cho target đó. Không xóa history để tái đếm.

## Non-exam

LearningSummaryService giữ luồng legacy: tăng attempt/completion, cập nhật điểm khi có, max progress, cộng duration,derive status/snapshot theo mapper. PASSED/COMPLETED là success; SUBMITTED non-exam cũng theo nhánh success hiện có. Null score không tự ghi latest/best về0. Không suy summary là ledger học câu đúng duy nhất.

## Phạm vi hiển thị

SummaryController/OverviewService/InternalResultService đọc learning_summaries. Chỉ tiêu raw summary không cùng normalized analytics, không áp EXP caps hoặc projection exclusion journal. History giữ record nguồn; analytics/EXP dùng typed V2 subset theo view riêng. Không ép ba nguồn phải có cùng count hoặc cùng đơn vị điểm.

API và nullable response xem [History & Summary](api-blueprint/history_summary_api_blueprint.md), [Analytics](api-blueprint/analytics.md).
