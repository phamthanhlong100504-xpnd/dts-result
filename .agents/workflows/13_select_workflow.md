---
description: Guide the agent to analyze the user request and recommend the most appropriate workflow to follow.
---

# Select Workflow

## Purpose

Analyze the user's request and recommend/select the most appropriate workflow to ensure a structured, rule-compliant implementation.

This serves as the entry point for handling any user request.

---

## Input

- User Request / Prompt
- Project State (files open, active workspace)

---

## Steps

### Step 1 — Analyze User Intent

Parse the user's request and categorize the primary goal into one of the following intentions:

1. **Understand/Explore Code:** User asks how something works, where a feature is located, or wants to explore the codebase.
2. **Analyze Requirements:** User provides raw business requirements and wants to clarify them.
3. **Design APIs:** User wants to define endpoints, requests, responses, or error contracts.
4. **Implement Code:** User wants to generate new classes, controllers, services, or configurations from an existing design.
5. **Review Quality:** User wants to check code for standards, rules, or code smells.
6. **Refactor Code:** User wants to clean up, simplify, or restructure existing code without changing behavior.
7. **Investigate Bug:** User reports an error, stack trace, exception, or unexpected behavior.
8. **Fix Bug:** User wants to apply a fix for a known issue.
9. **Write Tests:** User wants to add unit, integration, or API tests.
10. **Write Docs:** User wants to generate readmes, APIs, or architectural docs.
11. **Design Architecture:** User wants to plan modules, communication patterns, or database schemas.
12. **End-to-End Feature:** User wants to develop a complete new feature from requirements to tested code.

---

### Step 2 — Select the Appropriate Workflow

Recommend the workflow matching the analyzed intent based on the mapping below:

| User Intent / Scenario | Recommended Workflow | Key Input Needed |
|---|---|---|
| "Giải thích file này hoạt động thế nào", "Tìm nơi xử lý logic X" | [01_read_code.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/01_read_code.md) | Source code |
| "Phân tích yêu cầu nghiệp vụ này", "Xem yêu cầu này đủ thông tin chưa" | [02_understand_requirement.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/02_understand_requirement.md) | Business requirements |
| "Thiết kế API cho chức năng X", "Tạo API contract" | [03_generate_blueprint.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/03_generate_blueprint.md) | Requirements summary |
| "Sinh code Java cho API này", "Viết class theo blueprint" | [04_generate_code.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/04_generate_code.md) | Approved API Blueprint |
| "Review chất lượng code file này", "Xem code có vi phạm rule không" | [05_review_code.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/05_review_code.md) | Target code |
| "Tối ưu/dọn dẹp method này", "Refactor package X" | [06_refactor_code.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/06_refactor_code.md) | Clean code to improve |
| "Tại sao lại lỗi NPE ở dòng X", "Tìm nguyên nhân lỗi này" | [07_debug_issue.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/07_debug_issue.md) | Stack trace & logs |
| "Viết unit test cho Service X", "Thêm test case cho Controller Y" | [08_generate_test.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/08_generate_test.md) | Target production code |
| "Viết tài liệu hướng dẫn sử dụng folder X", "Tạo tài liệu API" | [09_generate_documentation.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/09_generate_documentation.md) | Implementation code |
| "Thiết kế mô hình module cho hệ thống", "Lựa chọn công nghệ X hay Y" | [10_design_architecture.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/10_design_architecture.md) | Architecture goals |
| "Phát triển tính năng X từ đầu đến cuối", "Thêm chức năng đăng ký học" | [11_feature_development.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/11_feature_development.md) | Full requirements |
| "Sửa lỗi crash khi click button X", "Fix lỗi trùng lặp dữ liệu" | [12_bug_fix.md](file:///C:/Users/Dai/Desktop/media/.agents/workflows/12_bug_fix.md) | Known bug & code |

---

### Step 3 — Guide the User

Present the recommendation to the user in the following format:

1. **Workflow đề xuất:** Tên và link tới file workflow.
2. **Lý do lựa chọn:** Giải thích ngắn gọn tại sao workflow này phù hợp với yêu cầu của họ.
3. **Thông tin cần cung cấp thêm (nếu thiếu):** Yêu cầu bổ sung logs, code hoặc requirements nếu chưa đủ preconditions.
4. **Các bước tiếp theo:** Hướng dẫn người dùng cách chạy hoặc phê duyệt bước đầu tiên của workflow đó.

---

## Output

- Recommended Workflow Link
- Rationale
- Clarification questions (if info is missing)
- Next Steps instruction
