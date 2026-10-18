# Result — DTO field reference

Field lấy từ source, không thay Bean Validation/service hoặc JSON example đầy đủ. Nullable wrapper khác primitive. Các form legacy tồn tại không chứng minh có HTTP controller tương ứng. Xem từng blueprint về scope/security/đơn vị và response rỗng.

## EvaluateCriteriaRequest

Nguồn [EvaluateCriteriaRequest](../../src/main/java/com/dts/result/api/form/EvaluateCriteriaRequest.java).

| Field | Kiểu |
| --- | --- |
| nodeId | `UUID` |
| triggerSource | `String` |

## QuestionAnswerForm

Nguồn [QuestionAnswerForm](../../src/main/java/com/dts/result/api/form/QuestionAnswerForm.java).

| Field | Kiểu |
| --- | --- |
| questionId | `UUID` |
| questionVersionId | `UUID` |
| answer | `Map<String, Object>` |
| isCorrect | `Boolean` |
| score | `BigDecimal` |
| maxScore | `BigDecimal` |

## RecordLearningLogRequest

Nguồn [RecordLearningLogRequest](../../src/main/java/com/dts/result/api/form/RecordLearningLogRequest.java).

| Field | Kiểu |
| --- | --- |
| nodeId | `UUID` |
| contentId | `UUID` |
| contentType | `String` |
| sessionKind | `String` |
| startedAt | `OffsetDateTime` |
| endedAt | `OffsetDateTime` |
| durationSec | `Integer` |
| mediaPositionSec | `Integer` |
| documentPageRead | `Integer` |
| deviceKind | `String` |
| metadata | `Map<String, Object>` |

## RecordTrackingEventRequest

Nguồn [RecordTrackingEventRequest](../../src/main/java/com/dts/result/api/form/RecordTrackingEventRequest.java).

| Field | Kiểu |
| --- | --- |
| eventType | `String` |
| entityKind | `String` |
| entityId | `UUID` |
| versionId | `UUID` |
| versionNo | `Integer` |
| language | `String` |
| blockId | `UUID` |
| nodePath | `String` |
| occurredAt | `OffsetDateTime` |
| source | `String` |
| idempotencyKey | `String` |
| payload | `Map<String, Object>` |
| context | `Map<String, Object>` |

## SaveNoteRequest

Nguồn [SaveNoteRequest](../../src/main/java/com/dts/result/api/form/SaveNoteRequest.java).

| Field | Kiểu |
| --- | --- |
| contentId | `UUID` |
| nodeId | `UUID` |
| contentVersionId | `UUID` |
| mediaTimestampSec | `Integer` |
| documentPage | `Integer` |
| noteText | `String` |

## SaveReactionRequest

Nguồn [SaveReactionRequest](../../src/main/java/com/dts/result/api/form/SaveReactionRequest.java).

| Field | Kiểu |
| --- | --- |
| contentId | `UUID` |
| contentType | `String` |
| reactionType | `String` |

## SubmitAttemptRequest

Nguồn [SubmitAttemptRequest](../../src/main/java/com/dts/result/api/form/SubmitAttemptRequest.java).

| Field | Kiểu |
| --- | --- |
| nodeId | `UUID` |
| contentId | `UUID` |
| contentType | `ContentType` |
| startedAt | `OffsetDateTime` |
| submittedAt | `OffsetDateTime` |
| durationSec | `Integer` |
| timeTakenSec | `Integer` |
| deviceKind | `String` |
| sessionId | `UUID` |
| contentVersionId | `UUID` |
| sourceService | `String` |
| sourceRef | `String` |
| answers | `List<QuestionAnswerForm>` |
| proctoringData | `Map<String, Object>` |

## ToggleBookmarkRequest

Nguồn [ToggleBookmarkRequest](../../src/main/java/com/dts/result/api/form/ToggleBookmarkRequest.java).

| Field | Kiểu |
| --- | --- |
| contentId | `UUID` |
| contentType | `String` |
| nodeId | `UUID` |
| note | `String` |

## AttemptDetailResponse

Nguồn [AttemptDetailResponse](../../src/main/java/com/dts/result/api/response/AttemptDetailResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| userId | `UUID` |
| nodeId | `UUID` |
| contentId | `UUID` |
| contentType | `String` |
| seqNo | `Integer` |
| startedAt | `OffsetDateTime` |
| endedAt | `OffsetDateTime` |
| submittedAt | `OffsetDateTime` |
| status | `String` |
| gradingStatus | `String` |
| durationSec | `Integer` |
| timeTakenSec | `Integer` |
| deviceKind | `String` |
| sessionId | `UUID` |
| contentVersionId | `UUID` |
| sourceService | `String` |
| sourceRef | `String` |
| score | `BigDecimal` |
| maxScore | `BigDecimal` |
| penaltyScore | `BigDecimal` |
| finalScore | `BigDecimal` |
| isPassed | `Boolean` |
| isLate | `Boolean` |
| hintUsedCount | `Integer` |
| answers | `Object` |
| proctoringData | `Object` |
| createdAt | `OffsetDateTime` |
| updatedAt | `OffsetDateTime` |

## AttemptSummaryResponse

Nguồn [AttemptSummaryResponse](../../src/main/java/com/dts/result/api/response/AttemptSummaryResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| userId | `UUID` |
| nodeId | `UUID` |
| contentId | `UUID` |
| contentType | `String` |
| seqNo | `Integer` |
| startedAt | `OffsetDateTime` |
| submittedAt | `OffsetDateTime` |
| status | `String` |
| gradingStatus | `String` |
| score | `BigDecimal` |
| maxScore | `BigDecimal` |
| finalScore | `BigDecimal` |
| isPassed | `Boolean` |
| timeTakenSec | `Integer` |
| createdAt | `OffsetDateTime` |

## AttemptTrend

Nguồn [AttemptTrend](../../src/main/java/com/dts/result/api/response/AttemptTrend.java).

| Field | Kiểu |
| --- | --- |
| date | `String` |
| attempts | `long` |

## BookmarkResponse

Nguồn [BookmarkResponse](../../src/main/java/com/dts/result/api/response/BookmarkResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| userId | `UUID` |
| contentId | `UUID` |
| contentType | `String` |
| nodeId | `UUID` |
| note | `String` |
| isBookmarked | `Boolean` |
| createdAt | `OffsetDateTime` |

## CompletionTrend

Nguồn [CompletionTrend](../../src/main/java/com/dts/result/api/response/CompletionTrend.java).

| Field | Kiểu |
| --- | --- |
| date | `String` |
| completed | `long` |

## ContentResultResponse

Nguồn [ContentResultResponse](../../src/main/java/com/dts/result/api/response/ContentResultResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| userId | `UUID` |
| contentId | `UUID` |
| contentType | `String` |
| contentVersionId | `UUID` |
| parentNodeId | `UUID` |
| contentCode | `String` |
| status | `String` |
| percent | `Integer` |
| totalLearnSec | `Integer` |
| learnCount | `Integer` |
| attemptCount | `Integer` |
| bestScore | `BigDecimal` |
| lastScore | `BigDecimal` |
| lastScoreAt | `OffsetDateTime` |
| lastAttemptId | `UUID` |
| answers | `Object` |
| metadata | `Object` |
| startedAt | `OffsetDateTime` |
| lastActivityAt | `OffsetDateTime` |
| completedAt | `OffsetDateTime` |
| createdAt | `OffsetDateTime` |
| updatedAt | `OffsetDateTime` |

## CriteriaResultResponse

Nguồn [CriteriaResultResponse](../../src/main/java/com/dts/result/api/response/CriteriaResultResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| criteriaId | `UUID` |
| userId | `UUID` |
| seqNo | `Integer` |
| isLatest | `Boolean` |
| status | `String` |
| totalScore | `BigDecimal` |
| isPassed | `Boolean` |
| gradeLabel | `String` |
| items | `Object` |
| graderId | `UUID` |
| graderNote | `String` |
| gradedAt | `OffsetDateTime` |
| evaluatedAt | `OffsetDateTime` |
| createdAt | `OffsetDateTime` |
| updatedAt | `OffsetDateTime` |

## ErrorResponse

Nguồn [ErrorResponse](../../src/main/java/com/dts/result/api/response/ErrorResponse.java).

| Field | Kiểu |
| --- | --- |
| code | `String` |
| message | `String` |
| details | `List<String>` |
| traceId | `String` |
| timestamp | `OffsetDateTime` |

## HistoryDetailResponse

Nguồn [HistoryDetailResponse](../../src/main/java/com/dts/result/api/response/HistoryDetailResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| sourceType | `String` |
| sourceId | `UUID` |
| activityType | `String` |
| displayTitle | `String` |
| licenseClass | `String` |
| correctCount | `Integer` |
| questionCount | `Integer` |
| resultAvailable | `Boolean` |
| reviewAvailable | `Boolean` |
| reasonCode | `String` |
| sourceResult | `SourceResultResponse` |
| targetType | `String` |
| targetId | `UUID` |
| attemptNo | `Integer` |
| result | `String` |
| score | `BigDecimal` |
| maxScore | `BigDecimal` |
| progress | `BigDecimal` |
| durationSeconds | `Integer` |
| startedAt | `Instant` |
| completedAt | `Instant` |
| resultSnapshot | `Map<String, Object>` |
| metadata | `Map<String, Object>` |

## HistoryItemResponse

Nguồn [HistoryItemResponse](../../src/main/java/com/dts/result/api/response/HistoryItemResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| sourceType | `String` |
| sourceId | `UUID` |
| activityType | `String` |
| displayTitle | `String` |
| licenseClass | `String` |
| correctCount | `Integer` |
| questionCount | `Integer` |
| resultAvailable | `Boolean` |
| reviewAvailable | `Boolean` |
| reasonCode | `String` |
| sourceResult | `SourceResultResponse` |
| targetType | `String` |
| targetId | `UUID` |
| attemptNo | `Integer` |
| result | `String` |
| score | `BigDecimal` |
| maxScore | `BigDecimal` |
| progress | `BigDecimal` |
| durationSeconds | `Integer` |
| startedAt | `Instant` |
| completedAt | `Instant` |

## LeaderboardResponse

Nguồn [LeaderboardResponse](../../src/main/java/com/dts/result/api/response/LeaderboardResponse.java).

| Field | Kiểu |
| --- | --- |
| licenseClass | `String` |
| period | `String` |
| timezone | `String` |
| asOf | `Instant` |
| from | `Instant` |
| to | `Instant` |
| currentUser | `Entry` |
| items | `List<Entry>` |
| totalParticipants | `long` |
| page | `int` |
| size | `int` |
| totalPages | `long` |
| ruleVersion | `String` |
| questionCaptureStartedAt | `Instant` |

## Entry

Nguồn [LeaderboardResponse](../../src/main/java/com/dts/result/api/response/LeaderboardResponse.java).

| Field | Kiểu |
| --- | --- |
| userId | `UUID` |
| displayName | `String` |
| avatarUrl | `String` |
| profileStatus | `String` |
| rank | `Long` |
| rankingScore | `long` |
| lastCreditedAt | `Instant` |

## LearningLogResponse

Nguồn [LearningLogResponse](../../src/main/java/com/dts/result/api/response/LearningLogResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| userId | `UUID` |
| nodeId | `UUID` |
| contentId | `UUID` |
| contentType | `String` |
| sessionKind | `String` |
| startedAt | `OffsetDateTime` |
| endedAt | `OffsetDateTime` |
| durationSec | `Integer` |
| mediaPositionSec | `Integer` |
| documentPageRead | `Integer` |
| deviceKind | `String` |
| metadata | `Object` |
| createdAt | `OffsetDateTime` |

## LessonNoteResponse

Nguồn [LessonNoteResponse](../../src/main/java/com/dts/result/api/response/LessonNoteResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| userId | `UUID` |
| contentId | `UUID` |
| nodeId | `UUID` |
| contentVersionId | `UUID` |
| mediaTimestampSec | `Integer` |
| documentPage | `Integer` |
| noteText | `String` |
| createdAt | `OffsetDateTime` |
| updatedAt | `OffsetDateTime` |

## OverviewResponse

Nguồn [OverviewResponse](../../src/main/java/com/dts/result/api/response/OverviewResponse.java).

| Field | Kiểu |
| --- | --- |
| completedPrograms | `long` |
| inProgressPrograms | `long` |
| completedChapters | `long` |
| completedLessons | `long` |
| averageScore | `BigDecimal` |
| bestScore | `BigDecimal` |
| totalLearningTimeSeconds | `long` |
| totalAttempts | `long` |
| totalExamsTaken | `long` |
| passedExams | `long` |
| failedExams | `long` |
| lastActivityAt | `Instant` |

## ProgressResponse

Nguồn [ProgressResponse](../../src/main/java/com/dts/result/api/response/ProgressResponse.java).

| Field | Kiểu |
| --- | --- |
| targetType | `String` |
| total | `long` |
| completed | `long` |
| inProgress | `long` |
| notStarted | `long` |
| completionRate | `BigDecimal` |
| averageProgress | `BigDecimal` |

## ReactionResponse

Nguồn [ReactionResponse](../../src/main/java/com/dts/result/api/response/ReactionResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| userId | `UUID` |
| contentId | `UUID` |
| contentType | `String` |
| reactionType | `String` |
| createdAt | `OffsetDateTime` |
| updatedAt | `OffsetDateTime` |

## RecentActivityResponse

Nguồn [RecentActivityResponse](../../src/main/java/com/dts/result/api/response/RecentActivityResponse.java).

| Field | Kiểu |
| --- | --- |
| targetType | `String` |
| targetId | `UUID` |
| sourceId | `UUID` |
| result | `String` |
| score | `BigDecimal` |
| completedAt | `Instant` |

## ResultAnalyticsResponse

Nguồn [ResultAnalyticsResponse](../../src/main/java/com/dts/result/api/response/ResultAnalyticsResponse.java).

| Field | Kiểu |
| --- | --- |
| licenseClass | `String` |
| period | `String` |
| timezone | `String` |
| asOf | `Instant` |
| from | `Instant` |
| to | `Instant` |
| dataStatus | `String` |
| trendMode | `String` |
| scoreTrendTruncated | `boolean` |
| scoreTrendTotalDays | `long` |
| scoreTrendLimit | `int` |
| summary | `Summary` |
| scoreTrend | `List<TrendPoint>` |
| recentResults | `List<RecentResult>` |

## Summary

Nguồn [ResultAnalyticsResponse](../../src/main/java/com/dts/result/api/response/ResultAnalyticsResponse.java).

| Field | Kiểu |
| --- | --- |
| averageScore | `BigDecimal` |
| maxScore | `Integer` |
| scoreBasis | `String` |
| passRate | `BigDecimal` |
| accuracyRate | `BigDecimal` |
| attempts | `long` |
| correctCount | `long` |
| questionCount | `long` |

## TrendPoint

Nguồn [ResultAnalyticsResponse](../../src/main/java/com/dts/result/api/response/ResultAnalyticsResponse.java).

| Field | Kiểu |
| --- | --- |
| date | `String` |
| score | `BigDecimal` |
| averageScore | `BigDecimal` |
| attempts | `long` |

## RecentResult

Nguồn [ResultAnalyticsResponse](../../src/main/java/com/dts/result/api/response/ResultAnalyticsResponse.java).

| Field | Kiểu |
| --- | --- |
| resultId | `UUID` |
| title | `String` |
| activityType | `String` |
| licenseClass | `String` |
| result | `String` |
| score | `BigDecimal` |
| maxScore | `Integer` |
| sourceScore | `BigDecimal` |
| sourceMaxScore | `BigDecimal` |
| correctCount | `Integer` |
| questionCount | `Integer` |
| completedAt | `Instant` |
| resultAvailable | `Boolean` |
| reviewAvailable | `Boolean` |
| reasonCode | `String` |
| sourceResult | `SourceResultResponse` |

## ResumeResponse

Nguồn [ResumeResponse](../../src/main/java/com/dts/result/api/response/ResumeResponse.java).

| Field | Kiểu |
| --- | --- |
| targetType | `String` |
| targetId | `UUID` |
| progress | `BigDecimal` |
| lastActivityAt | `Instant` |

## ScoreTrend

Nguồn [ScoreTrend](../../src/main/java/com/dts/result/api/response/ScoreTrend.java).

| Field | Kiểu |
| --- | --- |
| date | `String` |
| averageScore | `BigDecimal` |

## SourceResultResponse

Nguồn [SourceResultResponse](../../src/main/java/com/dts/result/api/response/SourceResultResponse.java).

| Field | Kiểu |
| --- | --- |
| sourceType | `String` |
| sourceId | `UUID` |
| resultPath | `String` |

## StatisticsResponse

Nguồn [StatisticsResponse](../../src/main/java/com/dts/result/api/response/StatisticsResponse.java).

| Field | Kiểu |
| --- | --- |
| scoreTrend | `List<ScoreTrend>` |
| studyTimeTrend | `List<StudyTimeTrend>` |
| attemptTrend | `List<AttemptTrend>` |
| completionTrend | `List<CompletionTrend>` |

## StudyTimeTrend

Nguồn [StudyTimeTrend](../../src/main/java/com/dts/result/api/response/StudyTimeTrend.java).

| Field | Kiểu |
| --- | --- |
| date | `String` |
| durationSeconds | `long` |

## SubmitAttemptResponse

Nguồn [SubmitAttemptResponse](../../src/main/java/com/dts/result/api/response/SubmitAttemptResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| userId | `UUID` |
| contentId | `UUID` |
| contentType | `ContentType` |
| seqNo | `Integer` |
| startedAt | `OffsetDateTime` |
| submittedAt | `OffsetDateTime` |
| status | `String` |
| gradingStatus | `String` |
| score | `BigDecimal` |
| maxScore | `BigDecimal` |
| penaltyScore | `BigDecimal` |
| finalScore | `BigDecimal` |
| isPassed | `Boolean` |
| timeTakenSec | `Integer` |
| createdAt | `OffsetDateTime` |

## SummaryDetailResponse

Nguồn [SummaryDetailResponse](../../src/main/java/com/dts/result/api/response/SummaryDetailResponse.java).

| Field | Kiểu |
| --- | --- |
| targetType | `String` |
| targetId | `UUID` |
| status | `String` |
| attemptCount | `Integer` |
| completionCount | `Integer` |
| bestScore | `BigDecimal` |
| latestScore | `BigDecimal` |
| averageScore | `BigDecimal` |
| progress | `BigDecimal` |
| totalDurationSeconds | `Integer` |
| lastActivityAt | `Instant` |
| completedAt | `Instant` |
| summarySnapshot | `Map<String, Object>` |
| metadata | `Map<String, Object>` |

## SummaryItemResponse

Nguồn [SummaryItemResponse](../../src/main/java/com/dts/result/api/response/SummaryItemResponse.java).

| Field | Kiểu |
| --- | --- |
| targetType | `String` |
| targetId | `UUID` |
| status | `String` |
| attemptCount | `Integer` |
| bestScore | `BigDecimal` |
| latestScore | `BigDecimal` |
| averageScore | `BigDecimal` |
| progress | `BigDecimal` |
| lastActivityAt | `Instant` |

## SummaryStatusResponse

Nguồn [SummaryStatusResponse](../../src/main/java/com/dts/result/api/response/SummaryStatusResponse.java).

| Field | Kiểu |
| --- | --- |
| completed | `long` |
| inProgress | `long` |
| notStarted | `long` |

## TrackingEventResponse

Nguồn [TrackingEventResponse](../../src/main/java/com/dts/result/api/response/TrackingEventResponse.java).

| Field | Kiểu |
| --- | --- |
| id | `UUID` |
| tenantId | `UUID` |
| userId | `UUID` |
| eventType | `String` |
| entityKind | `String` |
| entityId | `UUID` |
| versionId | `UUID` |
| versionNo | `Integer` |
| language | `String` |
| blockId | `UUID` |
| nodePath | `String` |
| occurredAt | `OffsetDateTime` |
| receivedAt | `OffsetDateTime` |
| source | `String` |
| idempotencyKey | `String` |
| payload | `Object` |
| context | `Object` |
| createdAt | `OffsetDateTime` |
