# Data maintenance tường minh — không phải API frontend

Các file SQL/psql sau được commit để thành viên có quy trình dữ liệu lặp lại được. Chúng không chạy khi start app/migrate schema, và không được chạy chỉ để commit hoặc để làm đẹp leaderboard. Không có packet dữ liệu user thật, token hoặc lệnh auto-apply trong tài liệu này.

## Điều chỉnh nguồn projection

[record-projection-adjustment.psql](../db/record-projection-adjustment.psql) nhận psql variables expected_database,adjustment_event_id,source_type,source_id,expected_source_hash,excluded,reason_code,evidence_ref. source_type PRACTICE_QUESTION/PRACTICE_SESSION/EXAM_SESSION; reason VERIFIED_TEST_DATA/SOURCE_REVOKED/SOURCE_RESTORED theo constraint.

Operator xác minh đúng một source cụ thể và MD5(to_jsonb(row)::text) hiện tại. Script kiểm database, advisory lock source và hash, append journal result_projection_adjustments. Cùng eventId chỉ lặp cùng quyết định, mismatch lỗi/rollback; repeat không tăng sequence cho event đã ghi. Không sửa/xóa learning_results/study_question_facts hoặc History.

View exclusions lấy quyết định mới nhất theo adjustment_sequence, excluded=true loại source khỏi EXP/analytics. Không ảnh hưởng History/Summary legacy, không loại bằng regex title/username. Khôi phục là quyết định mới có bằng chứng, không DELETE journal append-only.

Hash manifest của file này được đồng bộ với bytes local hiện hành trong đợt Git:08FE… cũ →619E… hiện tại. Đây không phải sửa V001–V004 hoặc history checksum; toàn bộ SQL được giữ nguyên.

## Bootstrap profile còn thiếu

[20261016__bootstrap_missing_public_profiles.psql](../db/data-migrations/20261016__bootstrap_missing_public_profiles.psql) là data migration riêng. Cần đúng expected_database, session_user postgres và V004/checksum chuẩn. packet_base64 là UTF-8 JSON array1..100 snapshot tối thiểu, không phải SQL được eval từ frontend.

Mỗi item chỉ có userId,displayName,sourceUpdatedAt,sourceTimestamp,observedAt,adminUserId,adminHttpVerified,evidenceRef. Operator phải đọc dữ liệu thật qua Identity admin GET /api/v1/admin/users/{id}; script kiểm shape/timestamp/flag/evidence, không tự gọi Identity hoặc tự chứng thực admin JWT. Snapshot không quá10 phút, đồng bộ sourceTimestamp=floor(updatedAt epoch-ms), ID không zero và không trùng.

Chỉ insert missing row, không ghi đè row đã có; verify mismatch/concurrent row sẽ rollback. Không tạo object schema, không đổi migration đã chạy, không bịa user-event. Trước áp dụng business database cần backup/restore-clone proof và kế hoạch nguồn cụ thể theo quy trình vận hành nhóm; gói Git không thực hiện bước này.

## Không trộn với migration schema

Canonical Apply chỉ V001–V004. Bootstrap/adjustment là lựa chọn dữ liệu theo bằng chứng từng môi trường, không cần mọi thành viên copy các UUID test đã có ở máy khác. Ba TIMEOUT bất thường trước đây tiếp tục giữ nguyên, không đổi thành PASSED/FAILED để lấp history.
