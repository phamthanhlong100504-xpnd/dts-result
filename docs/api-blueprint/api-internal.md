# Internal Result APIs

| Method | Path trực tiếp | Chức năng |
| --- | --- | --- |
| GET | /internal/results/users/{userId}/overview | Overview của user chỉ định |
| GET | /internal/results/users/{userId}/summaries | List SummaryItemResponse, optional targetType/status |

InternalResultController dùng userId path, không owner scope từ JWT. SecurityConfig hiện permitAll /internal/**; không có shared key/client credential mới tại controller. Giữ tuyến này trong mạng backend phù hợp với cơ chế triển khai hiện có. Gateway ResultInternalRouteGuard không cho proxy đường internal, không dùng nó làm API frontend chọn user khác.

Overview/summaries dùng nguồn legacy learning_summaries; không phải analytics normalized hoặc leaderboard. Không có endpoint nội bộ ở đây để sửa điểm nguồn, publish result, set EXP hoặc bootstrap profile. Xem [legacy APIs](legacy_apis.md), [contract notes](contract_notes.md).
