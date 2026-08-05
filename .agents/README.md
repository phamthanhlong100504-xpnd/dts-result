# Agent Customizations (`.agents/`)

Thư mục này chứa các thiết lập tùy chỉnh (Customizations) dành cho AI Coding Assistant nhằm hướng dẫn Agent tuân thủ nghiêm ngặt các quy chuẩn thiết kế, kiến trúc và quy trình nghiệp vụ của dự án.

---

## 1. Cấu trúc thư mục

Thư mục `.agents/` được tổ chức thành 2 phần chính:

### `/rules/` (Bộ Quy Tắc)
Chứa các quy tắc chuẩn hóa code và thiết kế hệ thống, phân chia theo các nhóm công nghệ và kiến trúc:
*   **`architecture/`**: Định nghĩa chuẩn kiến trúc (Clean Architecture, DDD, Event-driven, Microservices, Modular Monolith).
*   **`docx/`**: Quy chuẩn sinh tài liệu thiết kế API Blueprint (`docx/java/api-blueprint-generator.md`).
*   **`global/`**: Bộ quy tắc nền tảng áp dụng chung (Đặt tên `NAME-xxx`, API `API-xxx`, Database, Security, Error Handling, Logging, Git...).
*   **`infrastructure/`**: Tiêu chuẩn cấu hình và kết nối hạ tầng (PostgreSQL, Docker, Kubernetes, Kafka, Redis...).
*   **`technology/`**: Tiêu chuẩn viết code của ngôn ngữ và framework (Java Core `JAVA-xxx`, JPA/Hibernate `PERSIST-xxx`, Spring Boot, Testing).
*   **`templates/`**: Bản mẫu (Template) cấu trúc package và các class chuẩn (Controller, Service, Entity, Mapper...).

### `/workflows/` (Quy Trình Làm Việc)
Chứa 13 quy trình hướng dẫn chi tiết từng bước cho Agent khi thực hiện các tác vụ phát triển phần mềm cụ thể.

---

## 2. Các Tính Năng & Workflows được hỗ trợ

Bộ quy trình này giúp Agent phối hợp nhịp nhàng với bộ quy tắc thông qua các workflow từ đầu đến cuối:

1.  **Quản lý Quy Tắc (`00_select_rules.md`)**: Tự động xác định và tải đúng bộ quy tắc cần thiết cho từng tác vụ nhằm tối ưu ngữ cảnh.
2.  **Đọc Hiểu Mã Nguồn (`01_read_code.md`)**: Hướng dẫn phân tích cấu trúc dự án hiện tại dựa trên các quy tắc kiến trúc và đặt tên.
3.  **Làm Rõ Yêu Cầu (`02_understand_requirement.md`)**: Phân tích yêu cầu nghiệp vụ từ khách hàng mà không tự ý phỏng đoán, kiểm tra tính hợp lệ của thiết kế API/Database.
4.  **Thiết Kế API Blueprint (`03_generate_blueprint.md`)**: Sinh tài liệu thiết kế API độc lập với ngôn ngữ (tuân thủ `API-BLUEPRINT-xxx`).
5.  **Sinh Code Tự Động (`04_generate_code.md`)**: Sinh code Java Spring Boot chuẩn chỉ theo các template, áp dụng quy tắc Lombok, JPA, clean architecture.
6.  **Đánh Giá Mã Nguồn (`05_review_code.md`)**: Review code và phát hiện các lỗi vi phạm, bắt buộc chỉ ra mã quy tắc cụ thể (ví dụ: `PERSIST-028a`).
7.  **Tái Cấu Trúc Code (`06_refactor_code.md`)**: Cải tiến chất lượng mã nguồn một cách an toàn mà không làm thay đổi hành vi nghiệp vụ.
8.  **Tìm và Sửa Lỗi (`07_debug_issue.md` & `12_bug_fix.md`)**: Định vị nhanh root cause, kiểm tra xem có vi phạm quy tắc không và sửa lỗi an toàn.
9.  **Viết Test Tự Động (`08_generate_test.md`)**: Sinh các test case (Unit, Integration, Controller) toàn diện theo chuẩn JUnit 5.
10. **Tài Liệu Hóa (`09_generate_documentation.md`)**: Tạo tài liệu kỹ thuật khớp 100% với code thực tế.
11. **Thiết Kế Kiến Trúc (`10_design_architecture.md`)**: Thiết kế module/service và đánh giá rủi ro kiến trúc.
12. **Phát Triển Tính Năng Khép Kín (`11_feature_development.md`)**: Bộ điều phối (Coordinator) chạy xuyên suốt từ Phân tích yêu cầu → Viết Blueprint → Sinh code → Viết Test → Review.
13. **Lựa chọn Quy Trình (`13_select_workflow.md`)**: Phân tích yêu cầu người dùng và hướng dẫn chọn workflow phù hợp nhất để xử lý.
14. **Sinh DB Schema (`14_generate_db_schema.md`)**: Sinh file SQL schema PostgreSQL từ yêu cầu nghiệp vụ, tuân thủ `DB-SCHEMA-xxx`. Output lưu tại `docs/db/`.

---

## 3. Thư mục `docs/` và `plan/` (Project-specific Output)

Ngoài `.agents/` (repo chung — tái sử dụng cho nhiều dự án), mỗi dự án cụ thể cần tạo các thư mục ngang cấp với `.agents/` để lưu các sản phẩm thiết kế:

```
<project-root>/
├── .agents/          ← Repo chung (rules, workflows) — TÁI SỬ DỤNG
├── docs/             ← Riêng cho từng dự án
│   ├── api-blueprint/  ← API Blueprint files
│   └── db/             ← SQL schema files
├── plan/             ← Riêng cho từng dự án — Implementation Plans
```

*   **`docs/api-blueprint/`**: Chứa các file Markdown API Blueprint được sinh bởi workflow `03_generate_blueprint`.
*   **`docs/db/`**: Chứa các file `.sql` schema được sinh bởi workflow `14_generate_db_schema`.
*   **`plan/`**: Chứa các Implementation Plans đã được phê duyệt.

---

## 4. Quy tắc lưu Plan

Khi một Implementation Plan được người dùng **phê duyệt**, agent PHẢI tự động lưu bản copy vào thư mục `plan/` của dự án.

### Format tên file

```
DD-MM-YYYY-HH-mm-<mô-tả-ngắn>.md
```

Ví dụ: `20-07-2026-15-20-tai-cau-truc-package.md`

### Quy tắc

- **PHẢI** lưu plan ngay sau khi người dùng phê duyệt, trước khi bắt đầu thực hiện.
- **PHẢI** dùng timestamp tại thời điểm phê duyệt (múi giờ local của người dùng).
- **PHẢI** dùng tiếng Việt không dấu cho phần mô tả ngắn, phân cách bằng dấu gạch ngang.
- **KHÔNG** xoá plan cũ — luôn giữ lại lịch sử.

---

## 5. Cài Đặt Vào Project (Git Submodule)

Repo này được thiết kế để **tái sử dụng** cho nhiều dự án. Mỗi project sẽ import repo này vào thư mục `.agents/` dưới dạng **Git Submodule**.

### 🚀 Cách 1: Chạy script tự động

**Linux / macOS / Git Bash (Windows):**
```bash
# Chạy từ thư mục gốc project đích
curl -sSL https://raw.githubusercontent.com/phamthanhlong100504-xpnd/doc-manual/main/scripts/setup.sh | bash

# Hoặc nếu đã clone repo này:
bash path/to/doc-manual/scripts/setup.sh
```

**Windows PowerShell:**
```powershell
# Chạy từ thư mục gốc project đích
.\path\to\doc-manual\scripts\setup.ps1

# Với URL tùy chỉnh:
.\path\to\doc-manual\scripts\setup.ps1 -RepoUrl "https://github.com/phamthanhlong100504-xpnd/doc-manual.git"
```

### 🔧 Cách 2: Lệnh thủ công

```bash
# 1. Thêm submodule
cd /path/to/your-project
git submodule add -b main https://github.com/phamthanhlong100504-xpnd/doc-manual.git .agents

# 2. Commit
git add .gitmodules .agents
git commit -m "chore: add AI-Coding agents submodule"
```

### 📦 Clone project có submodule

```bash
# Lần đầu clone (bao gồm submodule)
git clone --recurse-submodules https://github.com/your-org/your-project.git

# Nếu đã clone rồi mà chưa có .agents/
git submodule init && git submodule update
```

### 🔄 Cập nhật lên bản mới nhất

**Script tự động:**
```bash
# Linux / macOS / Git Bash
bash .agents/scripts/update.sh
```
```powershell
# Windows PowerShell
.\.agents\scripts\update.ps1
```

**Lệnh thủ công:**
```bash
git submodule update --remote .agents
git add .agents
git commit -m "chore: update AI-Coding agents"
```

### 📌 Checkout version cụ thể

```bash
bash .agents/scripts/update.sh --version v1.2.0
```
```powershell
.\.agents\scripts\update.ps1 -Version "v1.2.0"
```

---

## 6. Cách Sử Dụng Dành Cho Lập Trình Viên

Khi bạn trò chuyện với AI Coding Assistant trong workspace này:
1.  **Tự động nhận diện:** Agent sẽ tự động phát hiện thư mục `.agents/` này làm Workspace Customizations Root và tải các quy tắc.
2.  **Kích hoạt Workflow:** Bạn có thể yêu cầu Agent chạy một workflow cụ thể bằng cách gõ tên hoặc gọi lệnh (ví dụ: *"Hãy dùng workflow 11_feature_development để phát triển tính năng X"*).
3.  **Tuân thủ quy chuẩn:** Agent sẽ luôn thực hiện **Step 0 — Load Rules** của mỗi workflow để đọc các file quy tắc trước khi tạo ra bất kỳ thay đổi nào trên code của bạn.
4.  **Lưu Plan tự động:** Khi plan được phê duyệt, agent sẽ tự động lưu vào `plan/` trước khi bắt tay vào thực hiện.

