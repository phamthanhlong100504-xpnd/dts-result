# DTS Result — schema và data migration

## Canonical schema

Database dts_result_db, history dts_result_schema_history, manifest Revision3. Không dùng Flyway. Runner tools/screen12-schema.ps1 hoặc screen12_schema.py kiểm file SHA trước apply-schema.psql; SQL database guard/advisory lock/single transaction/history/checksum rồi verify. Boot không tự chạy schema này.

| Version | Nghiệp vụ |
| --- | --- |
| V001__baseline_entity_schema.sql | learning_results và learning_summaries theo baseline; trigger/functions/index/compatibility |
| V002__screen12_contract_integrity.sql | Ràng buộc và verification dữ liệu Result nền |
| V003__screen13_exam_history.sql | Typed activity/title/license/counts/release/event version, legacy EXAM enrichment và index History |
| V004__screen14_exp_and_screen16_analytics.sql | study_question_facts,practice_exp_policy_events,result_projection_adjustments,leaderboard_public_profiles; views/scales/credits/analytics indexes |

V003 có update metadata EXAM cũ trong transaction với updated_at trigger tạm disable/enable theo SQL; không mô tả mọi version là chỉ tạo bảng. V004 additive projection giữ source results/summaries, có trigger chống UPDATE/DELETE ba evidence/journal tables. View/function tính credit theo scope, không tự backfill correctness cũ.

## Checksum và đóng gói

Manifest quản lý20 file, gồm SQL/SCREEN12_SCHEMA.md/runner/record-projection-adjustment.psql. Snapshot local có19 hash khớp và1 hash cũ ở maintenance entrypoint; gói Git chỉ thay value của entry đó:08FE986D… →619E7E2B…. SQL file hiện tại đã có từ trước được giữ nguyên. V001–V004 hashes trong manifest/apply/verify và checksum version ghi history không đổi. Không sửa history hoặc dùng repair để né kiểm tra.

.gitattributes hiện có giữ docs/db/** và hai runner LF. Không renormalize toàn repo. schema-manifest.json cần commit bản metadata đã đồng bộ cùng SQL thật để máy mới không bị runner từ chối ngay preflight. docs/db/SCREEN12_SCHEMA.md được giữ nguyên byte; hướng dẫn mới đặt riêng tại đây.

## Data maintenance riêng

record-projection-adjustment.psql append exclusion/restoration journal với exact source/hash/evidence. data-migrations/20261016__bootstrap_missing_public_profiles.psql chỉ insert minimal verified missing profiles. Hai thao tác không chạy khi Apply/boot, không tạo schema mới và không sửa kết quả học tập. Xem [maintenance](../api-blueprint/projection_maintenance.md); tests/public-profile-bootstrap-smoke.psql là fixture riêng, không seed vào business DB.

## Giới hạn schema legacy

RequiredTables của manifest là2 bảng result/summary và4 bảng projection. Tám entity table legacy user_content_results,user_content_attempts,user_criteria_results,content_bookmarks,content_reactions,lesson_notes,learning_logs,tracking_events không tự được tạo chỉ vì class tồn tại. Đọc đúng tên @Table tại source khi triển khai nhóm đó; không bịa DDL hoặc bật Hibernate update trong đợt Git. Các API History/leaderboard/analytics dùng schema canonical nêu trên.

Máy mới dùng đúngdatabase/credential/runner theo [LOCAL_SETUP](../LOCAL_SETUP.md). Môi trường có dữ liệu cần backup/upgrade proof trước Apply theo quy trình nhóm; bước chuẩn bị Git không chạy SQL/clone/data maintenance. Mọi schema thay đổi tiếp theo cần version migration mới, không sửa migration đã thực thi.
