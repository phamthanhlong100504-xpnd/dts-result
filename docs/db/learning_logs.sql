-- Table: learning_logs
-- Service: lms-tracking
-- Engine: clickhouse
-- Entities mapped: learning-log
-- Mô tả: Append-only log hợp nhất mọi hoạt động học tập của learner phục vụ analytics & báo cáo dài hạn trên ClickHouse. Gộp 4 nhóm sự kiện vào 1 bảng qua discriminator `event_type` (giống pattern `tracking_events`): (1) tiến độ xem video, (2) hoàn thành node, (3) kết quả làm bài, (4) vòng đời ghi danh. Các cột dimension dùng chung (user/enrollment/course/lesson/courseware) là cột thật để ORDER BY/filter; field riêng từng loại nằm trong `details` JSON, truy vấn bằng JSONExtract. Tách khỏi `tracking_events` (YugabyteDB, source-of-truth ~30 ngày hot): bảng này là lớp cold/analytics ghi qua Kafka — nguồn ghi gồm cả data mới (heartbeat video có position_seconds mà tracking_events không có) lẫn sink từ các service (lms-exam emit quiz tại GRADED→TRANSFERRED trước khi purge; lms-access emit enrollment lifecycle). Append-only — không updated_at/deleted_at/version.
-- Kafka topic: lms-tracking.learning_logs
--
-- Quy ước details (JSON) theo event_type:
--   video_progress / video_complete : {"position_seconds":int,"duration_seconds":int,"watched_seconds":int,"percent_watched":float,"reason":"heartbeat|pause|seek"}
--   lesson_complete / courseware_complete / section_complete : {"learn_time_seconds":int,"completed_at":"<iso8601>"}
--   quiz_attempt    : {"attempt_number":int,"score":float,"max_score":float,"pass_score":float,"is_passed":bool,"duration_seconds":int,"answers":[{"question_id","is_correct","score"}]}
--   enroll/unenroll/course_complete/course_expire/course_suspend : {"reason":string}

CREATE TABLE IF NOT EXISTS learning_logs (
    -- I. ĐỊNH DANH & TENANCY
    id          UUID,           -- Khóa chính (UUID v7).
    tenant_id   UUID,           -- Tenant sở hữu dữ liệu.

    -- II. LOẠI SỰ KIỆN
    event_type Enum8(           -- Phân loại hoạt động học tập; quyết định schema của details.
        'video_progress'      = 1,  -- Heartbeat/pause/seek khi xem video (chi tiết ở details.reason).
        'video_complete'      = 2,  -- Xem hết video / đạt ngưỡng.
        'lesson_complete'     = 3,  -- Hoàn thành bài học.
        'courseware_complete' = 4,  -- Hoàn thành 1 học liệu.
        'section_complete'    = 5,  -- Hoàn thành 1 chương/phần.
        'quiz_attempt'        = 6,  -- 1 lần làm bài kiểm tra (kết quả ở details).
        'enroll'              = 7,  -- Đăng ký khóa học.
        'unenroll'            = 8,  -- Hủy đăng ký.
        'course_complete'     = 9,  -- Hoàn thành khóa học.
        'course_expire'       = 10, -- Hết hạn truy cập khóa.
        'course_suspend'      = 11  -- Tạm khóa truy cập.
    ),

    -- III. NGỮ CẢNH HỌC TẬP (cột thật để ORDER BY/filter; '0' nếu không áp dụng cho event_type đó)
    user_id       UUID,         -- Learner bị tác động.
    actor_id      UUID DEFAULT '00000000-0000-0000-0000-000000000000', -- Người thực hiện (khác user_id khi admin thao tác hộ); thường dùng cho enroll/unenroll/suspend.
    enrollment_id UUID DEFAULT '00000000-0000-0000-0000-000000000000', -- Lượt ghi danh liên quan.
    course_id     UUID DEFAULT '00000000-0000-0000-0000-000000000000', -- Khóa học liên quan.
    lesson_id     UUID DEFAULT '00000000-0000-0000-0000-000000000000', -- Bài học liên quan (event cấp lesson/courseware/quiz).
    courseware_id UUID DEFAULT '00000000-0000-0000-0000-000000000000', -- Học liệu liên quan (video/quiz/courseware).

    -- IV. DỮ LIỆU RIÊNG TỪNG LOẠI
    details String DEFAULT '{}', -- JSON field riêng theo event_type (xem quy ước ở header); truy vấn bằng JSONExtract.

    -- V. CONTEXT (chủ yếu cho event ghi danh — audit)
    ip_address IPv6,             -- IP thiết bị (IPv6 bao quát IPv4-mapped).
    user_agent String DEFAULT '',-- Trình duyệt / thiết bị.

    -- VI. THỜI GIAN & TRUY VẾT
    created_at     DateTime64(3, 'UTC') DEFAULT now(), -- Thời điểm phát sinh sự kiện (UTC, ms).
    correlation_id String DEFAULT '',                 -- Trace id xuyên service do gateway sinh.

    INDEX idx_course_lookup     course_id     TYPE bloom_filter(0.01) GRANULARITY 1,
    INDEX idx_courseware_lookup courseware_id TYPE bloom_filter(0.01) GRANULARITY 1
)
ENGINE = MergeTree()
-- Phân vùng theo tháng để purge retention bằng drop partition.
PARTITION BY toYYYYMM(created_at)
-- Tối ưu cho query chủ đạo: "Dòng thời gian hoạt động loại Y của learner X".
ORDER BY (tenant_id, user_id, event_type, created_at)
SETTINGS index_granularity = 8192;
