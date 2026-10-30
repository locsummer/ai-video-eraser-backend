# AI Video Eraser - Full Stack Ecosystem (Server + Android + iOS)

Hệ sinh thái mã nguồn hoàn chỉnh cho ứng dụng **AI Video Eraser** (tương thích iOS App Store [id6744909217](https://apps.apple.com/us/app/ai-video-eraser/id6744909217?l=vi), Android và Web).

Bao gồm **3 thành phần hoàn chỉnh, độc lập và kết nối chặt chẽ**:
1. 🖥️ **`server/` — Backend Server:** Java 21, Spring Boot 3.3.5, Spring Security 6, JWT, Spring Data JPA, H2/PostgreSQL, OpenAPI 3 / Swagger, GPU Task Queue & Inpainting Worker API.
2. 📱 **`android/` — Android App:** 100% Kotlin, Jetpack Compose, Material 3, AndroidX Media3 (ExoPlayer), Retrofit 2, Coroutines, StateFlow.
3. 🍎 **`ios/` — iOS App:** 100% Swift, SwiftUI, AVKit, URLSession Async/Await, Canvas Mask Drawing, Xcode Project sẵn sàng.

---

## 1. Cấu trúc toàn bộ Repository

```
ai-video-eraser/
├── server/                            # 🖥️ BACKEND SERVER (Spring Boot / Java 21)
│   ├── src/main/java/com/aivideoeraser/
│   │   ├── AiVideoEraserApplication.java
│   │   ├── config/ (Security, OpenAPI, DataInitializer)
│   │   ├── controller/ (Auth, Video, Tasks, Worker, Plans, Admin, Health)
│   │   ├── service/ (Auth, Video, EraseTask, Feedback, Storage, Admin)
│   │   ├── entity/ (User, Video, EraseTask, Subscription, Feedback)
│   │   ├── repository/ (Spring Data JPA Repositories)
│   │   ├── worker/ (InpaintingWorkerService - GPU Worker Queue)
│   │   └── security/ (JWT, BCrypt, UserDetails)
│   ├── src/main/resources/ (application.yml, application-prod.yml)
│   ├── src/test/java/ (17 bài Unit & Integration tests)
│   ├── pom.xml                        # Maven configuration
│   ├── Dockerfile                     # Docker build cho Server
│   └── README.md                      # Hướng dẫn chi tiết Server
├── android/                           # 📱 ANDROID APP (Kotlin / Jetpack Compose)
│   ├── app/src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/aivideoeraser/android/
│   │       ├── MainActivity.kt
│   │       ├── data/ (api, model, repository)
│   │       ├── ui/ (screens, components, theme, navigation)
│   │       └── viewmodel/ (Auth, Editor, Task)
│   ├── build.gradle.kts & gradlew.bat
│   └── README.md                      # Hướng dẫn chi tiết Android
├── ios/                               # 🍎 IOS APP (Swift / SwiftUI)
│   ├── AIVideoEraser.xcodeproj/       # File dự án Xcode
│   ├── AIVideoEraser/
│   │   ├── AIVideoEraserApp.swift
│   │   ├── Views/ (Home, Editor, Processing, Result, Subscription, Auth)
│   │   ├── ViewModels/ & Services/
│   │   └── Info.plist
│   └── README.md                      # Hướng dẫn chi tiết iOS
├── docker-compose.yml                 # Khởi chạy Backend Server + PostgreSQL 16
└── README.md                          # Tài liệu tổng quan
```

---

## 2. Các tính năng cốt lõi trên toàn hệ thống

- **Xóa vật thể video bằng AI (AI Object Removal):** Người dùng đánh dấu vùng chọn, Server tiếp nhận tác vụ và điều phối qua hàng đợi GPU Cloud (mô hình ProPainter, E2FGVI) để inpainting và duy trì sự nhất quán giữa các frame (temporal consistency).
- **Interactive Mask Tools:**
  - 🎯 **AI Object Tracking (Track AI):** Chạm để khóa và bám theo đối tượng di chuyển.
  - ◻️ **Custom Rectangle:** Khung chữ nhật tùy chỉnh kích thước và vị trí.
  - ⬭ **Custom Ellipse:** Vùng chọn hình elip/tròn.
  - 🖌️ **Freehand Brush:** Cọ vẽ tự do kèm thanh trượt điều chỉnh độ dày nét cọ.
- **Video Trim Slider:** Cắt ngắn phân đoạn video trước khi xử lý (tương thích hạn mức Free 15s / Pro 60s / VIP 300s).
- **Màn hình Tiến độ thời gian thực (Real-time Progress):**
  - Vòng tròn % tiến độ động (0% - 100%).
  - Đếm frame xử lý: `Frame: 150 / 300`.
  - Cập nhật giai đoạn: *Trích xuất frame -> Bám mask AI -> Inpainting nền -> Re-encoding video*.
  - Nút Hủy task (tự động hoàn tiền/credits ngay lập tức).
- **Trình phát video & Đánh giá (Player & Feedback):**
  - Phát video kết quả với chuẩn **HTTP 206 Byte-Range Streaming** mượt mà trên AVPlayer (iOS) và ExoPlayer (Android).
  - Lưu video sạch về Camera Roll / Thư viện máy.
  - Đánh giá sao (1 - 5 sao) phản hồi chất lượng xóa.
- **Hệ thống Credits & VIP Membership:** Gói Free Starter (50 credits), Pro Monthly, Pro Yearly và VIP Lifetime.

---

## 3. Hướng dẫn khởi chạy từng thành phần

### 1. Khởi chạy Backend Server
```bash
cd server
mvn clean test
mvn spring-boot:run
```
- Swagger UI Interactive Docs: `http://localhost:8080/swagger-ui/index.html`
- Health Check: `http://localhost:8080/api/v1/health`
- Tài khoản Admin: `admin@aivideoeraser.app` / `Admin@123456`
- Tài khoản VIP Demo: `demo@aivideoeraser.app` / `Demo@123456`

Hoặc chạy toàn bộ hệ thống bằng Docker Compose (gồm PostgreSQL 16):
```bash
docker-compose up --build -d
```

### 2. Khởi chạy Android App
```bash
cd android
./gradlew.bat assembleDebug
```
Mở thư mục `android/` bằng Android Studio và nhấn **Run** (địa chỉ kết nối server mặc định cho giả lập là `http://10.0.2.2:8080/api/v1/`).

### 3. Khởi chạy iOS App
```bash
cd ios
open AIVideoEraser.xcodeproj
```
Mở dự án bằng **Xcode** trên macOS và nhấn `Cmd + R` để chạy trên Simulator hoặc iPhone thật.
