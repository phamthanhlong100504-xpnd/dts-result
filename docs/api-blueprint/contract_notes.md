# Result — contract và nguồn dữ liệu

## URL / response

Result local8086. Gateway8888 nhận /api/result/v1/** → /v1/**. Ví dụ /api/result/v1/results/me/history và /api/result/v1/leaderboards. SourceResult.resultPath là đường web /practice/result/{sourceId} hoặc /examination/result/{sourceId}, không phải URL REST để gọi trực tiếp.

Response DTO trực tiếp; History/Summary dùng Spring Page với content, leaderboard dùng items, analytics dùng summary/scoreTrend/recentResults. Không bọc data/success tự tạo. Tên field và đơn vị theo [DTO reference](dto_reference.md), [leaderboard](leaderboard.md), [analytics](analytics.md).

## Xác thực và ownership

/v1/results/me/** và /v1/leaderboards/** yêu cầu Bearer hợp lệ. Filter kiểm Base64 secret, issuer, sub UUID rồi đặt principal UUID; không dùng claim userId/header X-User-Id để chọn user. Query userId không làm analytics đổi chủ thể. Leaderboard công khai các trường xếp hạng tối thiểu của người tham gia, analytics/history chỉ của người gọi.

Source SecurityConfig permitAll /internal/** và anyRequest ngoài hai nhóm bảo vệ; không có shared-key middleware mới cho internal. Gateway ResultInternalRouteGuard chỉ cho proxy versioned /v1, nên internal/legacy unversioned không được mở qua proxy public này. Không tự suy tất cả endpoint trực tiếp trên8086 đều bắt JWT hoặc chỉ admin.

## Lỗi

Controller advice ErrorResponse có code,message,details,traceId,timestamp. traceId lấy X-Trace-Id hoặc traceparent, có thể null. AuthenticationEntryPoint trả401 chỉ code/message, không bắt buộc đủ trường advice.

| HTTP / code | Ý nghĩa |
| --- | --- |
|401 RES-401-001 | JWT thiếu/sai hoặc header bắt buộc theo handler |
|400 RES-400-001 | Validation/parameter/JSON theo các handler đã khai báo |
|404 RES-404-001 | History không tồn tại/không thuộc user/chưa release |
|500 RES-500-001 | Exception ngoài mapping hiện có |

Không hứa mọi IllegalArgumentException legacy là400 vì handler generic có thể trả500. Không dùng message làm enum nghiệp vụ. Body nullable và dataStatus/review flags là tín hiệu cần xử lý, không thay null normalized score thành grade0.

## Các phạm vi khác nhau

History giữ raw source snapshot, source-owned grade/title/review; EXP/Analytics chỉ typed V2 valid released EXAM và projection exclusions. Summary là projection học tập không cùng normalization/EXPcaps. Câu học đúng không tạo History. selectedLicenseClass từ Identity được frontend chọn khi gọi API yêu cầu licenseClass; Result không tự đọc profile để mặc định hạng.

licenseClass và period của API mới là giá trị chính xác, không tự trim/uppercase. Thiếu hoặc sai licenseClass400. Các path/source table legacy có contract riêng ở [legacy APIs](legacy_apis.md).
