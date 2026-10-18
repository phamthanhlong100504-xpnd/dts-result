# Chạy DTS Result trên máy mới

## 1. Công cụ và source

Dùng JDK21, Maven Wrapper đi kèm repo; pom dùng Spring Boot3.2.5. WrapperVersion3.3.4, distributionType=only-script, distribution Maven3.9.16 theo .mvn/wrapper/maven-wrapper.properties. Không cần thêm maven-wrapper.jar: wrapper hiện tại là only-script và jar đang được ignore có chủ đích. Main class com.dts.result.ResultApplication.

Nhánh bàn giao fix/result-persistence-and-kafka-retry cho tới khi nhóm merge về main. Ví dụ repo D:\DTS\dts-result.

```powershell
Set-Location -LiteralPath 'D:\DTS\dts-result'
java -version
```

## 2. Các backend liên quan

| Thành phần | Địa chỉ host-local | Vai trò |
| --- | --- | --- |
| Result | http://localhost:8086 | /v1/results/me/** và /v1/leaderboards |
| PostgreSQL16 | localhost:5438 / dts_result_db | History, summary, EXP evidence, profile cache |
| Kafka | localhost:9092 | learning-results và user-events |
| Identity | http://localhost:8081 | JWT và sự kiện tên công khai |
| Practice | http://localhost:8087 | Result outbox, correct-question evidence và source scale policy V30 |
| Examination | http://localhost:8088 | Official results đã release |
| Gateway | http://localhost:8888 | /api/result/v1/** → /v1/** |

Result đọc các event đã được publish; HTTP leaderboard/analytics không gọi từng user sang Identity hoặc gọi Practice để chấm lại. Kafka consumer Result cần nguồn Practice/Examination tương thích; thiếu source policy là SOURCE_POLICY_PENDING, không tự điền hạng/câu/điểm cố định.

## 3. Environment

Máy mới chưa có .env có thể sao chép mẫu và điền giá trị riêng. Máy đang chạy bình thường giữ cấu hình hiện tại.

```powershell
Set-Location -LiteralPath 'D:\DTS\dts-result'
Copy-Item -LiteralPath '.env.example' -Destination '.env'
notepad .env
```

SERVER_PORT=8086 phải được nạp: YAML nền có port8080, có thể trùng Media. EXPOSE8086 trong Dockerfile không tự override server.port. SPRING_DATASOURCE_URL phải đúng host port5438/database dts_result_db; YAML mặc định localhost5432 không phải cổng Result local của dự án. Java cùng mạng Docker dùng hostname/port nội bộ phù hợp, không copy nguyên localhost host-local vào container.

SPRING_DATASOURCE_USERNAME/PASSWORD là credential thật của database. Thay .env không đổi password volume cũ. JWT_SECRET phải Base64, thống nhất access secret Identity; filter Result giải mã Base64 trực tiếp và require issuer JWT_ISSUER. Không nhầm với key HTTP ingress Progress. SPRING_KAFKA_BOOTSTRAP_SERVERS là tên biến được YAML dùng; dùng giá trị localhost9092 cho Java trên host. EUREKA_CLIENT_ENABLED=false cho local đã cấu hình URL Gateway trực tiếp.

Maven/Spring Boot không tự nạp .env. Có thể đặt biến trong IDE hoặc nạp KEY=value vào terminal mà không chạy file như PowerShell:

```powershell
& {
    $envPath = Join-Path (Get-Location).Path '.env'
    foreach ($line in [IO.File]::ReadAllLines($envPath)) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) { continue }
        $pair = $line -split '=', 2
        if ($pair.Count -ne 2 -or $pair[0] -notmatch '^[A-Z][A-Z0-9_]*$') {
            throw 'Dòng .env không đúng KEY=value.'
        }
        [Environment]::SetEnvironmentVariable($pair[0], $pair[1], 'Process')
    }
}
```

Không thêm nháy ngoài value với loader trên. Biến mới chỉ áp dụng khi start tiến trình mới; không thay environment của Java đang chạy.

## 4. Database và schema trước khi boot máy mới

Dùng database/container DTS hiện có. Môi trường chưa có phải được tạo database dts_result_db và role/credential tương ứng; không tạo container trùng cổng/tên hoặc down -v volume hiện có. Giữ backup khi nâng cấp môi trường chứa dữ liệu và đọc [Migration catalog](db/MIGRATION_CATALOG.md).

Result không dùng Flyway. Hai entrypoint lựa chọn:

**Windows và PostgreSQL container local đang có:**

```powershell
Set-Location -LiteralPath 'D:\DTS\dts-result'
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File '.\tools\screen12-schema.ps1' `
  -RepositoryRoot 'D:\DTS\dts-result' `
  -DatabaseContainer 'dts-result-postgres' `
  -Database 'dts_result_db' -DatabaseUser 'postgres' -Mode Apply
if ($LASTEXITCODE -ne 0) { throw 'Migration chưa đạt; dừng trước khi chạy ứng dụng.' }
```

Nếu Docker được bọc bởi function/alias khiến runner không lấy được native executable, dùng entrypoint Python/psql bên dưới với kết nối riêng của máy thay vì sửa runner có checksum hoặc đổi DB. Cần Docker Desktop local khỏe và đúng container; script schema thực hiện thao tác ghi khi Mode Apply.

**Python3 và psql, chọn thay cho cách Docker:**

```powershell
$env:PGHOST = 'localhost'
$env:PGPORT = '5438'
$env:PGUSER = 'postgres'
# Dùng PGPASSFILE riêng hoặc cơ chế xác thực psql có sẵn của máy.
py -3 '.\tools\screen12_schema.py' --database dts_result_db --mode apply
if ($LASTEXITCODE -ne 0) { throw 'Migration chưa đạt.' }
py -3 '.\tools\screen12_schema.py' --database dts_result_db --mode verify
if ($LASTEXITCODE -ne 0) { throw 'Schema verify chưa đạt.' }
```

PG* của psql độc lập SPRING_DATASOURCE_* của Java. Runner kiểm manifest, expected database, transaction/history/checksum; không chạy DDL rời rồi tự ghi history. Database có bảng cũ phải tương thích canonical schema; lỗi dừng để rà, không bỏ constraint hoặc đổi checksum migration đã chạy.

Giữ Hibernate ddl-auto=none như source hiện hành. Canonical V001–V004 bao phủ learning_results/learning_summaries và projection EXP/profile; không bao phủ mọi entity legacy còn trong repo. Không đổi sang update để tự sinh thêm bảng. Những API learning-log/tracking cũ cần schema legacy của môi trường tương ứng; snapshot này không có DDL versioned cho toàn bộ8 bảng legacy ngoài manifest. Điều đó không cản các API History/leaderboard/analytics dùng schema canonical, nhưng không được hiểu boot thành công là mọi endpoint legacy đã được cấp đủ bảng.

## 5. Start Result

Sau khi đã nạp environment và schema chuẩn:

```powershell
Set-Location -LiteralPath 'D:\DTS\dts-result'
.\mvnw.cmd spring-boot:run
```

Wrapper tải distribution nếu máy chưa cache; có thể dùng Maven21-compatible đã cài theo môi trường nhóm. Giữ terminal mở. Boot chạy Kafka consumers và có thể ghi facts/projection; không coi boot là thao tác read-only.

- Health: http://localhost:8086/actuator/health
- Leaderboard trực tiếp: /v1/leaderboards?licenseClass=A1&period=THIS_MONTH
- History trực tiếp: /v1/results/me/history
- Analytics trực tiếp: /v1/results/me/analytics?licenseClass=A1&period=30D
- Gateway đặt RESULT_SERVICE_URL=http://localhost:8086 trước khi khởi động; web dùng /api/result/v1/...

pom hiện không khai báo springdoc, không hứa Swagger URL có sẵn. API blueprint là tài liệu contract của repo. Dockerfile chỉ build src thành JAR, không tự chạy docs/db/tools; cần checkout/bộ migration cùng phiên bản để vận hành bước schema riêng.

## 6. Vì sao máy mới có thể chưa thấy dữ liệu

Source không mang theo user, kết quả, EXP, profile cache hoặc Kafka offsets/volume của máy người phát triển. Cần Identity JWT đúng issuer/key và Practice/Examination phát kết quả/câu hỏi đã công bố. Replay retained events theo cơ chế vận hành được nhóm xác nhận; không bịa event/grade để làm đầy UI.

Practice V30 cung cấp QUESTION_CORRECT_RECORDED và PRACTICE_EXP_SOURCE_READY. Policy event quyết định scale hiện tại, không lấy maxScore từ fixture frontend. Profile thiếu có displayName=null/profileStatus=PENDING; user-events retained hoặc maintenance snapshot admin đã xác minh mới lấp cache. Ba phiên TIMEOUT dữ liệu thời gian sai đã được giữ nguyên, không sửa trong đợt Git.

## 7. Data maintenance và phạm vi Git

record-projection-adjustment.psql và bootstrap_missing_public_profiles.psql là thao tác dữ liệu tường minh, không chạy lúc start. Không cần chạy chúng để commit hoặc để mọi máy mới có cùng dữ liệu kiểm thử. [Projection maintenance](api-blueprint/projection_maintenance.md) giải thích guard và trách nhiệm.

Gói Git chỉ thêm tài liệu/mẫu/ignore và đồng bộ một hash manifest với maintenance script hiện đã có. Không đổi Java/YAML/pom/SQL, không chạy migration/API/Kafka/Docker hoặc restart trong bước đóng gói. target/build là artifact ignored; context/plan và tài liệu nền được giữ vì chưa có bằng chứng là rác.
