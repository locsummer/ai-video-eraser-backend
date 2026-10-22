# AI Video Eraser - Backend Server (Production-Ready)

Hệ thống Backend Server hoàn chỉnh và sẵn sàng vận hành thực tế cho ứng dụng **AI Video Eraser** (hỗ trợ iOS App [id6744909217](https://apps.apple.com/us/app/ai-video-eraser/id6744909217?l=vi), Android App và Web Dashboard).

---

## 1. Tổng quan & Kiến trúc hệ thống

Ứng dụng xóa vật thể trong video (**AI Video Eraser**) đòi hỏi khối lượng tính toán AI (Inpainting & Temporal Mask Tracking) rất nặng, yêu cầu mô hình chuyên sâu (như **ProPainter**, **E2FGVI**, **FastInpaint**) chạy trên GPU Cloud (RunPod, Vast.ai, AWS EC2, GCP). Backend Server đóng vai trò:
- Tiếp nhận video tải lên, tiền xử lý, kiểm tra định dạng và trích xuất thông tin.
- Quản lý dữ liệu Mask (Bounding Box, AI Tracking points, Polygon, Ellipse, Freehand Brush).
- Quản lý hàng đợi tác vụ xử lý video (**GPU Task Queue**) với cơ chế bất đồng bộ (`@Async`, Scheduled, Thread Pool) và Webhook / Worker API giao tiếp với các GPU Pod ngoài.
- Xác thực & Phân quyền bảo mật cao bằng **Spring Security 6** và **JWT (JSON Web Token)**.
- Quản trị hạn ngạch, tín dụng (**Credits**) và gói thành viên (**Free, Pro, VIP**).
- Cung cấp tính năng Stream Video hỗ trợ chuẩn **HTTP 206 Partial Content (Byte Range)** giúp các player trên iOS (`AVPlayer`) và Android (`ExoPlayer`) tua mượt mà không bị giật lag.
- Giám sát sức khỏe hệ thống (**Health Check & Metrics**) và giao diện tài liệu chuẩn **OpenAPI 3 / Swagger UI**.

---

## 2. Cấu trúc mã nguồn

```
d:\ai-video-eraser\
├── src/main/java/com/aivideoeraser/
│   ├── AiVideoEraserApplication.java     # Entry point chính của Spring Boot
│   ├── config/
│   │   ├── SecurityConfig.java           # Cấu hình Spring Security, CORS, JWT Filter
│   │   ├── OpenApiConfig.java            # Cấu hình Swagger 3 / OpenAPI UI
│   │   └── DataInitializer.java          # Tự động khởi tạo Admin, Demo VIP, System Settings
│   ├── controller/
│   │   ├── AuthController.java           # API Đăng ký, Đăng nhập, Token Refresh, Profile
│   │   ├── VideoController.java          # API Upload, Danh sách video, Xóa video
│   │   ├── FileStreamController.java     # API Stream/Download video (HTTP 206 Partial Content)
│   │   ├── EraseTaskController.java      # API Tạo tác vụ xóa AI, Kiểm tra tiến độ, Hủy
│   │   ├── WorkerController.java         # API dành cho GPU Worker (RunPod/Vast.ai)
│   │   ├── SubscriptionController.java   # API Xem bảng giá gói Pro/VIP, Nâng cấp
│   │   ├── FeedbackController.java       # API Đánh giá chất lượng sau khi xóa
│   │   ├── AdminController.java          # API Quản trị hệ thống, Dashboard, Quản lý Users
│   │   └── HealthController.java         # API Kiểm tra trạng thái máy chủ & Disk Space
│   ├── service/
│   │   ├── AuthService.java              # Nghiệp vụ tài khoản, xác thực, đổi mật khẩu
│   │   ├── VideoService.java             # Nghiệp vụ lưu trữ & metadata video
│   │   ├── FileStorageService.java       # Quản lý tệp cục bộ / Sanitize tên file / Range streaming
│   │   ├── EraseTaskService.java         # Nghiệp vụ Erase Task, trừ/hoàn credits, validate
│   │   ├── SubscriptionService.java      # Nghiệp vụ gói thành viên, thời lượng tối đa
│   │   ├── FeedbackService.java          # Nghiệp vụ feedback & đánh giá sao
│   │   └── AdminService.java             # Nghiệp vụ thống kê & vận hành admin
│   ├── worker/
│   │   └── InpaintingWorkerService.java  # Engine mô phỏng / Hàng đợi nền xử lý Video AI
│   ├── repository/                       # Spring Data JPA Repositories
│   ├── entity/                           # JPA Database Entities & Enums
│   ├── dto/                              # Request, Response, Page & Error DTOs
│   ├── security/                         # JWT Token Provider, Principal & UserDetailsService
│   └── exception/                        # Centralized Global Exception Handler
├── src/main/resources/
│   ├── application.yml                   # Cấu hình môi trường dev (H2 file DB)
│   └── application-prod.yml              # Cấu hình môi trường production (PostgreSQL)
├── src/test/java/com/aivideoeraser/
│   ├── AiVideoEraserIntegrationTest.java # 11 Integration tests (MockMvc E2E)
│   ├── AuthServiceTest.java              # Unit tests cho Authentication & Password
│   └── EraseTaskServiceTest.java         # Unit tests cho Quota, Credits, Inpainting flow
├── pom.xml                               # Quản lý dependencies Maven (Java 21, Spring Boot 3.3.5)
├── Dockerfile                            # Multi-stage Docker build
└── docker-compose.yml                    # Khởi chạy đồng bộ PostgreSQL + Backend Server
```

---

## 3. Danh sách REST API & Endpoints

Tất cả các API nghiệp vụ đều có tiền tố `/api/v1`.

### 3.1. Authentication & User Profile (`/api/v1/auth`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Đăng ký tài khoản mới (tặng sẵn 50 free credits) |
| `POST` | `/api/v1/auth/login` | Public | Đăng nhập nhận JWT Access Token & Refresh Token |
| `POST` | `/api/v1/auth/refresh-token` | Public | Cấp lại Access Token mới khi token cũ hết hạn |
| `GET` | `/api/v1/auth/me` | Authenticated | Lấy thông tin user hiện tại, số credits, gói đang dùng |
| `PUT` | `/api/v1/auth/profile` | Authenticated | Cập nhật họ tên, avatar URL |
| `PUT` | `/api/v1/auth/change-password` | Authenticated | Đổi mật khẩu tài khoản |

### 3.2. Quản lý Video (`/api/v1/videos`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/api/v1/videos/upload` | Authenticated | Upload video nguồn (hỗ trợ .mp4, .mov, .avi, max 500MB) |
| `GET` | `/api/v1/videos/{videoId}` | Authenticated | Xem chi tiết thông tin video & link stream |
| `GET` | `/api/v1/videos` | Authenticated | Danh sách video của người dùng (phân trang, sắp xếp) |
| `DELETE` | `/api/v1/videos/{videoId}` | Authenticated | Xóa video nguồn và file trên đĩa |

### 3.3. Tác vụ Xóa vật thể AI (`/api/v1/tasks`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/api/v1/tasks/erase` | Authenticated | Khởi tạo tác vụ xóa AI (Erase Mode, Mask Data, Tracking, Trim) |
| `GET` | `/api/v1/tasks/{taskId}` | Authenticated | Xem trạng thái, % tiến độ, frame hiện tại, kết quả |
| `GET` | `/api/v1/tasks` | Authenticated | Lịch sử các task đã chạy của user (lọc status, mode) |
| `POST` | `/api/v1/tasks/{taskId}/cancel` | Authenticated | Hủy task đang trong hàng đợi (tự động hoàn credits) |
| `DELETE` | `/api/v1/tasks/{taskId}` | Authenticated | Xóa task khỏi danh sách lịch sử |

### 3.4. Stream & Tải Video (`/api/v1/files/stream`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/v1/files/stream/video/{videoId}` | Public | Stream video nguồn với HTTP Byte-range (hỗ trợ `?download=true`) |
| `GET` | `/api/v1/files/stream/result/{taskId}` | Public | Stream hoặc tải video kết quả đã xóa sạch vật thể |

### 3.5. GPU Worker & Webhook Integration (`/api/v1/worker`)
*Xác thực qua header `X-Worker-Key: aivideo-eraser-internal-worker-secret-key-2026`*
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/api/v1/worker/jobs/next` | Worker Key | GPU worker lấy job tiếp theo từ hàng đợi |
| `POST` | `/api/v1/worker/jobs/{taskId}/progress` | Worker Key | Worker cập nhật số frame đã xử lý và % tiến độ |
| `POST` | `/api/v1/worker/jobs/{taskId}/complete` | Worker Key | Worker báo hoàn thành render kèm URL video kết quả |
| `POST` | `/api/v1/worker/jobs/{taskId}/fail` | Worker Key | Worker báo lỗi (tự động hoàn tiền/credits cho user) |

### 3.6. Gói dịch vụ & Đăng ký VIP (`/api/v1`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/v1/plans` | Public | Danh sách các gói Free, Pro Monthly, Pro Yearly, VIP |
| `POST` | `/api/v1/subscriptions/subscribe` | Authenticated | Đăng ký/nâng cấp gói Pro/VIP |
| `GET` | `/api/v1/subscriptions/my-subscription` | Authenticated | Xem chi tiết gói thuê bao đang kích hoạt |

### 3.7. Đánh giá chất lượng (`/api/v1/tasks`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/api/v1/tasks/{taskId}/feedback` | Authenticated | Gửi đánh giá sao (1-5★) và bình luận chất lượng xóa |
| `GET` | `/api/v1/tasks/{taskId}/feedback` | Authenticated | Xem lại đánh giá của task |

### 3.8. Quản trị viên (`/api/v1/admin`)
*Chỉ dành cho tài khoản có vai trò `ROLE_ADMIN`*
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/v1/admin/dashboard` | Admin | Thống kê số lượng user, task, tỷ lệ thành công, queue GPU |
| `GET` | `/api/v1/admin/users` | Admin | Quản lý danh sách người dùng trong hệ thống |
| `PUT` | `/api/v1/admin/users/{userId}/status` | Admin | Khóa / Mở khóa / Tạm dừng tài khoản |
| `POST` | `/api/v1/admin/users/{userId}/credits` | Admin | Cộng thêm credits cho người dùng |
| `GET` | `/api/v1/admin/tasks` | Admin | Xem toàn bộ các task trong hệ thống |
| `POST` | `/api/v1/admin/tasks/{taskId}/requeue` | Admin | Đưa lại task lỗi vào hàng đợi để xử lý lại |
| `GET` | `/api/v1/admin/system-settings` | Admin | Xem các cấu hình hệ thống |
| `PUT` | `/api/v1/admin/system-settings` | Admin | Cập nhật cấu hình hệ thống |

### 3.9. Kiểm tra máy chủ (`/api/v1/health`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/v1/health` | Public | Trạng thái máy chủ, dung lượng ổ đĩa trống, số jobs trong queue |

---

## 4. Tài khoản mặc định hệ thống

Hệ thống được thiết lập sẵn tự động tạo 2 tài khoản khi khởi động:
1. **Quản trị viên (Admin):**
   - Email: `admin@aivideoeraser.app`
   - Mật khẩu: `Admin@123456`
   - Quyền: `ROLE_ADMIN`
   - Credits: 99,999

2. **Người dùng VIP Demo:**
   - Email: `demo@aivideoeraser.app`
   - Mật khẩu: `Demo@123456`
   - Quyền: `ROLE_PREMIUM` (VIP Lifetime)
   - Credits: 500

---

## 5. Hướng dẫn cài đặt & Khởi chạy

### Cách 1: Khởi chạy trực tiếp (Development Mode)
Yêu cầu Java 21 hoặc mới hơn:
```bash
# Biên dịch và chạy kiểm thử
mvn clean test

# Chạy server
mvn spring-boot:run
```
Khi chạy xong, truy cập:
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **API Docs:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Health Check:** [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/aivideoeraser`)

### Cách 2: Khởi chạy bằng Docker Compose (Production Mode với PostgreSQL)
```bash
docker-compose up --build -d
```
Hệ thống sẽ tự động khởi động một container PostgreSQL 16 và container Backend Spring Boot kết nối với nhau qua mạng nội bộ.

---

## 6. Kết quả Kiểm thử (Test Report)

Dự án đã được kiểm thử toàn diện với **17 bài test** (gồm cả Unit Tests và Integration Tests):
- **11 bài Integration Tests E2E** (`AiVideoEraserIntegrationTest`):
  1. `testRegisterUser`: Đăng ký tài khoản mới và nhận free credits.
  2. `testLoginUser`: Đăng nhập lấy JWT Bearer token.
  3. `testGetProfile`: Đọc hồ sơ người dùng và gói subscription.
  4. `testUploadVideo`: Upload file video mp4 và trích xuất thông tin.
  5. `testCreateEraseTask`: Tạo task xóa AI kèm mask coordinates, trừ credits hợp lệ.
  6. `testGetTaskDetail`: Kiểm tra trạng thái và tiến độ xử lý.
  7. `testGetPlans`: Kiểm tra bảng giá các gói Pro/VIP.
  8. `testHealthEndpoint`: Kiểm tra health check và dung lượng ổ cứng.
  9. `testWorkerFlow`: Kiểm tra luồng GPU worker cập nhật tiến độ và báo hoàn thành.
  10. `testSubmitFeedback`: Gửi đánh giá 5 sao sau khi hoàn thành.
  11. `testAdminEndpoints`: Kiểm tra quyền Admin Dashboard và User Management.
- **3 bài Unit Tests** (`AuthServiceTest`): Mã hóa mật khẩu, kiểm tra trùng email, đăng nhập.
- **3 bài Unit Tests** (`EraseTaskServiceTest`): Khấu trừ credits, kiểm tra hạn mức duration, cơ chế tự động hoàn credits khi hủy task.

**Kết quả:**
```
[INFO] Results:
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
