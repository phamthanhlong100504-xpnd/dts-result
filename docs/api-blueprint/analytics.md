# API Blueprint: GET /v1/results/me/analytics

Gateway: GET /api/result/v1/results/me/analytics. Bearer, user luôn từ JWT; query/header userId không đổi chủ thể.

## Query / response

licenseClass bắt buộc thực tế, đúng enum uppercase như leaderboard; period mặc định30D, hỗ trợ7D/30D/ALL. Không có query page/size/from/to/sort trong controller này.7D/30D là rolling duration, không phải từ đầu tuần/tháng.

Response ResultAnalyticsResponse gồm licenseClass,period,timezone,asOf,from,to,dataStatus,trendMode,scoreTrendTruncated,scoreTrendTotalDays,scoreTrendLimit,summary,scoreTrend,recentResults. timezone Asia/Ho_Chi_Minh; trendMode DAILY_AVERAGE.

| Summary field | Công thức / đơn vị |
| --- | --- |
| averageScore | AVG(correctCount/questionCount*current source scale),2 decimals |
| maxScore | question count của hạng từ Practice policy hiện tại |
| scoreBasis | NORMALIZED_CORRECT_COUNT |
| passRate | PASSED attempts/attempts*100,2 decimals |
| accuracyRate | SUM correctCount/SUM questionCount*100,2 decimals |
| attempts | Toàn bộ eligible EXAM facts trong scope, không cap EXP |
| correctCount/questionCount | Tổng count từ nguồn, không suy ra từ raw score |

Source grade raw Practice phần trăm/official trọng số không được trộn trực tiếp. PASSED/FAILED vẫn từ source, có thể FAILED do critical dù normalized score cao. Không tự xác định pass bằng điểm normalized.

## Chart / recent

TrendPoint date(ngày Việt Nam),score,averageScore,attempts; score và averageScore là cùng giá trị daily mean. Chỉ ngày có attempt, không tự tạo ngày0. Tối đa3660 ngày có dữ liệu gần nhất, trả ascending; truncated/totalDays/limit cho UI biết cắt chart, summary vẫn toàn scope.

RecentResult tối đa5 theo completedAt desc/resultId desc. Fields title/activity/license/result,score/maxScore normalized,sourceScore/sourceMaxScore raw,correctCount/questionCount,completedAt,resultAvailable/reviewAvailable/reasonCode/sourceResult. resultId mở owned detail History; sourceResult.resultPath điều hướng web nguồn. Không thay resultId bằng sourceId.

## Nullable và empty

Thiếu policy hạng => dataStatus SOURCE_POLICY_PENDING, averageScore/maxScore/chart score/recent normalized score có thể null. Counts, passRate, accuracy vẫn theo eligible source. READY với attempts0 trả zero summary, arrays rỗng; đây là trạng thái empty hợp lệ. Không ép mọi null thành0 hoặc hard-code scale25/30 ở frontend.

History có thể có V1/legacy hoặc source bị loại EXP/analytics; analytics chỉ screen14_exam_facts typed V2/released/counts/time/grade hợp lệ và không projection excluded. Không bắt số attempts luôn bằng tổng History hoặc số attempt được EXPcredit.
