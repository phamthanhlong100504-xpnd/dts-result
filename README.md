# DTS Result

Java21 / Spring Boot3.2.5 / Maven. Result lưu kết quả đã công bố từ Practice/Examination, tổng hợp lịch sử, leaderboard EXP và analytics; cache tên công khai từ Identity user-events. Source giữ cả các API summary/overview/statistics và learning-log/tracking legacy với contract riêng.

| Nội dung | Tài liệu |
| --- | --- |
| Cài đặt, environment và khởi động | [LOCAL_SETUP](docs/LOCAL_SETUP.md) |
| Nghiệp vụ và trách nhiệm source | [RESULT_FEATURES](docs/RESULT_FEATURES.md) |
| API hiện hành, lỗi và DTO | [API blueprints](docs/api-blueprint/README.md) |
| Canonical schema và data maintenance | [Migration catalog](docs/db/MIGRATION_CATALOG.md) |
| Công thức EXP/analytics gốc của Screen14/16 | [Backend contract](docs/api/SCREEN14_16_BACKEND.md) |
| Mẫu environment không chứa credential thật | [.env.example](.env.example) |

Nhánh bàn giao hiện tại fix/result-persistence-and-kafka-retry, nền main lúc review ccedb4f. Local API8086, PostgreSQL host5438/database dts_result_db, Kafka9092. Gateway8888 nhận /api/result/v1/** và rewrite thành /v1/** của Result.

Schema chạy bằng SQL/runner trong docs/db và tools, không dùng Flyway, không tự migrate khi boot. Git mang theo source/config template/migration, không mang theo credential, account, profile hoặc lịch sử của máy khác. Bước chuẩn bị Git giữ Java/YAML/pom/SQL nguyên bản local.
