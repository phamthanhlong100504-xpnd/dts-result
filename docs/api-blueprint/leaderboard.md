# API Blueprint: GET /v1/leaderboards

Gateway: GET /api/result/v1/leaderboards. Bearer hợp lệ, currentUser từ JWT. Không có endpoint frontend tự ghi EXP hoặc tự đặt tên người khác.

## Query

| Field | Contract |
| --- | --- |
| licenseClass | Bắt buộc thực tế: thiếu/null400; đúng uppercase như A1,A,B1,B,C1,C,D1,D2,D,BE,C1E,CE,D1E,D2E,DE,A2,B2,E,F |
| period | THIS_MONTH mặc định; THIS_WEEK,THIS_MONTH,ALL_TIME |
| page | Chuỗi integer0..2147483647, mặc định0 |
| size | Chuỗi integer1..100, mặc định10 |

Không hỗ trợ sort tự chọn; server quyết định EXP/rank/order. Response LeaderboardResponse có licenseClass,period,timezone,asOf,from,to,currentUser,items,totalParticipants,page,size,totalPages,ruleVersion,questionCaptureStartedAt. Entry gồm userId,displayName,avatarUrl,profileStatus,rank,rankingScore,lastCreditedAt.

## Scope và hiển thị

THIS_WEEK từ thứ Hai00:00 Việt Nam; THIS_MONTH từ ngày100:00. to là cuối kỳ và có thể sau asOf, nhưng credit query bị chặn tại min(to,asOf). ALL_TIME from/to null. RuleVersion EXP_2026_01, questionCaptureStartedAt có thể null khi chưa có policy evidence.

rankingScore là EXP backend, không tự tính từ số lần luyện ở browser. Thứ tự score desc,lastCreditedAt asc,userId asc. rank là ROW_NUMBER toàn cục kể cả qua trang, không tự đánh lại1..size. totalParticipants chỉ user có EXP dương và không bị profile deleted; missing profile vẫn có thể được xếp hạng PENDING.

currentUser luôn có object độc lập trang. Không EXP dương trả rankingScore0/rank null/lastCreditedAt null; items có thể rỗng. displayName nullable, PENDING chưa có profile, UNAVAILABLE đã biết thiếu tên, AVAILABLE có tên. avatarUrl hiện null. Không thay tên null thành tên suy từ UUID hoặc account khác.

## Quy tắc EXP

Official correct*10+200pass, cap hai attempt đầu của parent exam; Random correct*2+30pass, cap năm completion đầu/ngày Việt Nam; Preset chọn max EXP trong scope cho mỗi preset; câu học đúng1/câu duy nhất theo bank trong scope. Chi tiết tie/capture/exclusions ở [RESULT_FEATURES](../RESULT_FEATURES.md) và [Screen14/16 source contract](../api/SCREEN14_16_BACKEND.md).

Leaderboard có thể thay đổi khi event đến trễ, policy/profile cập nhật hoặc có journal adjustment đã xác minh. Frontend refresh đúng scope và dùng asOf hiển thị thời điểm dữ liệu; không cache chung giữa user/hạng/kỳ/trang.
