# Public profile projection cho leaderboard

## Nguồn và thứ tự

PublicProfileConsumer đọc result.public-profile.topic (mặc định user-events), group ${spring.kafka.consumer.group-id}-public-profiles-v1. publicProfileKafkaListenerContainerFactory dùng StringDeserializer key/value riêng, earliest và auto-startup theo spring.kafka.listener.auto-startup. Lỗi DB retry partition riêng với FixedBackOff1000ms/unlimited, không làm mất tên vì consumer ack qua lỗi tạm.

Envelope USER_CREATED/USER_UPDATED/USER_DELETED dùng userId UUID,timestamp integer epoch-ms>0,fullName string tùy sự kiện. Cache strip tên, blank thành null, tối đa255; deleted luôn name null. Payload không hợp lệ được bỏ qua và chỉ log partition/offset, không log toàn hồ sơ; eventType khác bỏ qua.

Row mới thay row cũ khi tuple (source_timestamp,source_event_order,source_partition,source_offset) lớn hơn. Event order CREATED1,UPDATED2,DELETED3 xử lý tie timestamp; event cũ không phục hồi tên đã xóa. Không đồng bộ email/password/token, không tạo avatar khi Identity chưa cấp.

## Contract UI

Leaderboard profileStatus AVAILABLE/PENDING/UNAVAILABLE, displayName nullable và avatarUrl null. Profile chưa có cache không có nghĩa user không tồn tại; họ vẫn có thể nằm ranking khi EXP dương. Profile deleted bị loại khỏi ranking; history nguồn không bị xóa bởi consumer này.

Không có API frontend public để write cache. User legacy mất retained events có thể được bổ sung qua maintenance file với snapshot admin Identity đã xác minh, xem [maintenance](projection_maintenance.md). Bootstrap dùng order0/partition-1/offset-1 để không giả Kafka receipt; Identity events cùng millisecond hoặc mới hơn tiếp tục ưu tiên.

Source: [PublicProfileProjectionService](../../src/main/java/com/dts/result/application/service/PublicProfileProjectionService.java), [PublicProfileKafkaConfig](../../src/main/java/com/dts/result/config/PublicProfileKafkaConfig.java).
