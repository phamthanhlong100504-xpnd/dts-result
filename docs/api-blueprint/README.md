# DTS Result API blueprints

Contract source local nhánh fix/result-persistence-and-kafka-retry, nền main ccedb4f. Path trực tiếp Result8086; Gateway8888 thêm /api/result trước /v1. Không áp một loại wrapper hoặc đơn vị điểm cho mọi API.

| Blueprint | Phạm vi |
| --- | --- |
| [Contract notes](contract_notes.md) | JWT subject, lỗi/nullable, Gateway và namespace |
| [History & Summary](history_summary_api_blueprint.md) | Filters/Page/source detail/release/owner, summary/resume |
| [Leaderboard](leaderboard.md) | Period/rank/currentUser/EXP/profile status |
| [Analytics](analytics.md) | Normalized metrics/rolling window/chart/recent/policy pending |
| [Consumer](consumer_blueprint.md) | V2 results, EXP evidence, source dedup và retry/DLT |
| [Public profiles](public_profiles.md) | Identity events và named consumer độc lập |
| [Projection maintenance](projection_maintenance.md) | Explicit bootstrap/journal, không là API client |
| [Legacy APIs](legacy_apis.md) | Overview/statistics/log/tracking, scope/schema khác |
| [Internal APIs](api-internal.md) | Direct backend routes chọn user, không public proxy |
| [DTO reference](dto_reference.md) | Field form/response hiện có |
| [API overview](api-overview.md) | Tóm tắt namespace |

[Local setup](../LOCAL_SETUP.md), [Business features](../RESULT_FEATURES.md), [Schema](../db/MIGRATION_CATALOG.md), [Screen14/16 source contract](../api/SCREEN14_16_BACKEND.md). Tài liệu mới mô tả code hiện có, không ghi nhận build/runtime/DB đã chạy trong bước chuẩn bị Git.
