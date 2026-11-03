# API Blueprint: Truy vấn danh sách kết quả học tập

## Part 0: API Identity

- **API Name**: List Learning Results
- **API Type**: Internal / Public
- **Module**: Result & Tracking Service
- **Feature**: Learning Results Query
- **Description**: Truy vấn lịch sử các lượt nộp/phiên học của người dùng có hỗ trợ phân trang.
- **Related Tables**: `learning_results`

---

## Part 1: API Contract

### Endpoint
`GET /api/v1/result-service/learning-results`

### HTTP Method
`GET`

### Request Headers
| Header Key | Type | Required | Description |
|---|---|---|---|
| `X-User-Id` | UUID | Mandatory | ID người dùng |

### Query Parameters
| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `page` | Integer | Optional | 1 | Trang hiện tại (1-based) |
| `size` | Integer | Optional | 20 | Số lượng bản ghi mỗi trang |

### Response (200 OK)
Trả về `PageResponse<LearningResultResponse>`.

---

## Part 2: Processing Spec

- **Controller**: Trích xuất `X-User-Id`, `page`, `size`.
- **Service**: Gọi repository phân trang theo `userId`, sắp xếp giảm dần theo `createdAt`.
- **Repository**: Query `findByUserId(userId, pageable)`.

---

## Part 3: Data Interaction

- **SELECT**: `SELECT r FROM LearningResultEntity r WHERE r.userId = :userId ORDER BY r.createdAt DESC`
