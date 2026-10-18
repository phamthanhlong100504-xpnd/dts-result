# API Overview — DTS Result

Index hiện hành tại [README](README.md). API Result có ba namespace khác nhau:

- /v1/results/me/**: JWT scoped read, History/Summary/Overview/Statistics/Analytics.
- /v1/leaderboards: JWT required, public minimal ranking và currentUser theo token.
- /internal/results/** và /api/v1/result-service/** legacy: contract nội bộ/header riêng, không public qua Gateway result guard.

Gateway8888: /api/result/v1/** → Result8086/v1/**. Các response DTO trực tiếp, Spring Page/content khác leaderboard/items. SourceResult.resultPath là đường điều hướng web, không là endpoint Result REST.

Đọc [History](history_summary_api_blueprint.md), [Leaderboard](leaderboard.md), [Analytics](analytics.md), [Consumer](consumer_blueprint.md), [Profile](public_profiles.md), [Maintenance](projection_maintenance.md), [Legacy](legacy_apis.md), [Internal](api-internal.md), [Contract](contract_notes.md).
