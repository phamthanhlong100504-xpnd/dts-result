# Screen 14 EXP leaderboard and Screen 16 analytics

This is the backend implementation. A local build/PostgreSQL report is required before deployment. Gateway routes already forward `/api/result/**` to this service; no route/configuration replacement is required.

## HTTP contract

Both endpoints require the existing Identity Bearer JWT. The signed UUID `sub` selects the current user. Caller-supplied user IDs/headers never select another user's analytics. Responses are direct JSON objects, without a `data` envelope. Invalid parameters return HTTP 400 / `RES-400-001`; anonymous/invalid JWT returns 401. Existing History and statistics endpoints keep their contracts.

| Gateway endpoint | Query | Defaults |
| --- | --- | --- |
| `GET /api/result/v1/leaderboards` | required `licenseClass`; `period=THIS_WEEK\|THIS_MONTH\|ALL_TIME`; integer `page>=0`, `size=1..100` | THIS_MONTH, page 0, size 10 |
| `GET /api/result/v1/results/me/analytics` | required `licenseClass`; `period=7D\|30D\|ALL` | 30D |

License validation matches History: A1, A, B1, B, C1, C, D1, D2, D, BE, C1E, CE, D1E, D2E, DE and legacy A2/B2/E/F. A legacy license without an authoritative current Practice scale returns `SOURCE_POLICY_PENDING`, never an invented scale.

## EXP rule EXP_2026_01

Every response uses one database statement/snapshot. Only released valid typed V2 EXAM facts from Screen13 contribute exam EXP. Grading/pass flags remain source-owned.

| Activity | Credit | Limit / selection |
| --- | --- | --- |
| OFFICIAL_EXAM | correctCount × 10 + 200 if PASSED | Earliest two actual delivered attempts per user/parent exam across all time, then filter completion period. This is a credit cap, never permission to retry. |
| RANDOM_EXAM | correctCount × 2 + 30 if PASSED | First five completions per user/license/Vietnam calendar day, ordered by completedAt then source UUID. Later attempts remain in History/Analytics. |
| PRESET_EXAM | correctCount × 2 + 30 if PASSED | One maximum-EXP attempt per user/license/preset **within each selected scope**. No sum of attempts. Tie: source score ratio, pass flag, earliest completion, source UUID. |
| Correct learning question | 1 | First correct `(user,license,bankVersion,questionId)` within selected scope, shared across chapter learning/review, critical practice, wrong review and PRACTICE mode. |

Confirmed preset decision: 21 correct/PASSED gives 72 EXP; a later 22 correct/critical FAILED gives 44 EXP and does not replace 72. All attempts remain source/history records.

THIS_WEEK starts Monday 00:00 Asia/Ho_Chi_Minh; THIS_MONTH starts day 1 at 00:00. Ranges are `[from,to)`, capped at response `asOf`. ALL_TIME has null from/to and the same asOf cutoff. Late official release belongs to source completedAt, not Kafka receipt time. No question evidence is reconstructed from old Progress payloads: those payloads do not prove correctness. `questionCaptureStartedAt` exposes the V30 capture boundary. Merely opening a traffic sign/card creates no graded evidence.

Ranking: EXP descending, latest contributing source timestamp ascending (first to achieve the tied score), user UUID ascending. `items[].rank` is global across pages. `currentUser` is returned even outside the page; no positive EXP => rank null, score 0. Participants count only positive EXP, nondeleted public profiles.

Names are projected from authoritative Identity `user-events` into a minimal cache. No email/phone/token is stored. Avatar is null because the current Identity source has no avatar. Missing event history gives displayName null/profileStatus PENDING; known missing name is UNAVAILABLE. A retained event or an independently verified administrator reconciliation is needed before claiming old profile coverage. No invented names or public profile-write API.

For legacy identities whose events expired, explicitly run `docs/db/data-migrations/20261016__bootstrap_missing_public_profiles.psql` after a restored-clone proof. Its `expected_database` variable pins the target; `packet_base64` contains a UTF-8 JSON array of minimal snapshots read through the existing administrator Identity GET `/api/v1/admin/users/{id}`. Each item contains only userId, displayName, sourceUpdatedAt, sourceTimestamp (floor of Identity updatedAt epoch milliseconds), observedAt, adminUserId, adminHttpVerified and evidenceRef. Snapshots expire after ten minutes. It inserts missing cache rows only and refuses mismatches; repeating preserves the entire existing row. It creates no schema objects, changes no executed schema migration and never runs on startup. Order 0 / partition -1 / offset -1 identifies this verified bootstrap without fabricating a Kafka event. Real Identity events at the same source millisecond or later continue to take precedence. `docs/db/tests/public-profile-bootstrap-smoke.psql` validates Unicode, idempotency and ordering on a uniquely owned clone only.

## Analytics units

Summary, daily scoreTrend and recentResults use identical user/license/period/release/exclusion scope. `7D` and `30D` are rolling durations of 7 and 30 × 24 hours. Daily grouping is Vietnam local date. ALL has no lower bound. Summary averages the per-attempt correct-count ratio normalized to the **current Practice-owned ExamMatrix question count**, delivered by the separate source policy event. It never mixes raw Practice percent scores with official question-count scores.

- attempts: all eligible completed attempts, including attempts outside EXP caps.
- passRate: PASSED attempts / attempts × 100, using the original pass flag including critical failure.
- accuracyRate: sum(correctCount) / sum(questionCount) × 100.
- averageScore: average(correctCount / questionCount × current source scale), rounded to 2 decimals; maxScore is that scale.
- trendMode DAILY_AVERAGE: each point has date, score/averageScore and attempts. No fabricated zero-day points. Latest 3660 observed days maximum; scoreTrendTruncated and scoreTrendTotalDays report truncation. Summary covers the entire requested range.
- recentResults: latest five by completedAt DESC/resultId DESC, same normalized score/maxScore as the chart; sourceScore/sourceMaxScore preserve raw grading units.
- Source policy unavailable: dataStatus SOURCE_POLICY_PENDING, normalized scores/maxScore null. Source-owned counts/pass rate/accuracy remain available. Empty known-policy scope returns zero KPIs and empty arrays.

Detail/navigation uses the existing Screen13 resultId and sourceResult.sourceId/resultPath (`/practice/result/{sourceId}` or `/examination/result/{sourceId}`). Do not grade from displayed normalized scores; preserve source result PASSED/FAILED and source review policy. Official unreleased facts never appear.

## Migration and ownership

Run the checked-in canonical `docs/db/apply-schema.psql` via the existing schema runner, after backup/clone proof, to apply V004. It includes immutable V001..V003 and the new V004 hash. Repeating skips recorded versions; modified executed checksums fail. There is no application startup DDL for these projections. Practice V30 is a separate prerequisite; see its documentation. Existing History rows, learning summaries, Progress payloads and grading records are not rewritten by V004.

Question and source-policy evidence are append-only/idempotent. The query computes scope-dependent credits deterministically; replay, concurrent duplicate ingestion and late arrival do not increment an additive total again. The existing Result retry/DLT transport handles the new envelopes. Profile consumption has its own named raw-JSON consumer factory/group; it does not replace the original learning-results deserializer.

`record-projection-adjustment.psql` is an explicit maintenance entrypoint for verified test-source exclusion/revocation/restoration. It requires the exact database, source UUID/type, current original-row MD5 and evidence reference. It appends a journal record; it never edits/deletes History. No broad title/username regex exclusion is permitted. The three earlier invalid TIMEOUT records remain untouched and are not converted into results.

## Validation before release

Meaningful PostgreSQL tests cover canonical fresh/repeat schema, source policy ordering, EXP caps and calendar scope, preset 72-vs-44 decision, context/replay/concurrency deduplication, independent license/bank namespaces, rankings/pages, mixed-scale analytics, ownership/release visibility, corrections and profile deletion. HTTP tests verify real JWT subject selection, anonymous denial and invalid queries. Kafka configuration tests verify separate profile/default factories and existing retry serialization. `DTS_SCREEN14_16_POSTGRES_TEST=true` enables real new PostgreSQL tests; `DTS_SCREEN13_POSTGRES_TEST=true` enables the History regression tests.

Compilation, these tests, actual business backup/clone upgrade proof, explicit migration and live Kafka/HTTP/runtime verification are separate gates. A successful source patch or Java syntax parse alone is not Screen14/16 completion. The current package does not deploy or run a business migration automatically.
