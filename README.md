# AI Video Eraser - Full Stack Ecosystem (Backend + Android + iOS)

Hệ sinh thái mã nguồn hoàn chỉnh cho ứng dụng **AI Video Eraser** (tương thích iOS App Store [id6744909217](https://apps.apple.com/us/app/ai-video-eraser/id6744909217?l=vi), Android và Web).

Bao gồm 3 phần độc lập, kết nối chặt chẽ qua chuẩn REST API:
1. **Backend Server (`/`):** Java 21, Spring Boot 3.3.5, Spring Security 6, JWT, Spring Data JPA, H2/PostgreSQL, OpenAPI 3, GPU Task Queue.
2. **Android Mobile App (`/android`):** 100% Kotlin, Jetpack Compose, Material 3, AndroidX Media3 (ExoPlayer), Retrofit 2, Coroutines, StateFlow.
3. **iOS Mobile App (`/ios`):** 100% Swift, SwiftUI, AVKit, URLSession Async/Await, Canvas Mask Drawing, Xcode Project sẵn sàng.

---

## 1. Cấu trúc toàn bộ Repository

```
ai-video-eraser/
├── src/main/java/com/aivideoeraser/   # Backend Server Spring Boot
├── pom.xml                            # Maven build configuration
├── Dockerfile & docker-compose.yml    # Container hóa Backend + PostgreSQL
├── android/                           # Ứng dụng Android (Kotlin)
│   ├── app/src/main/java/com/aivideoeraser/android/
│   │   ├── MainActivity.kt
│   │   ├── data/ (api, model, repository)
│   │   ├── ui/ (screens, components, theme, navigation)
│   │   └── viewmodel/ (Auth, Editor, Task)
│   ├── build.gradle.kts
│   └── README.md
└── ios/                               # Ứng dụng iOS (Swift)
    ├── AIVideoEraser.xcodeproj/       # File dự án Xcode
    ├── AIVideoEraser/
    │   ├── AIVideoEraserApp.swift
    │   ├── Models/
    │   ├── Services/
    │   ├── ViewModels/
    │   ├── Views/ (Home, Editor, Processing, Result, Subscription, Auth)
    │   └── Info.plist
    └── README.md
```

---

## 2. Tính năng chính trên 2 nền tảng Mobile (iOS Swift & Android Kotlin)

- **AI Object & People Removal:** Chọn và loại bỏ người đi ngang, watermark, chữ, chi tiết thừa trong video; AI tự động tái tạo nền nhất quán (temporal consistency).
- **Interactive Masking & Drawing Tools:**
  - 🎯 **AI Object Tracking:** Khóa và bám theo đối tượng di chuyển xuyên suốt các khung hình.
  - ◻️ **Custom Rectangle Mask:** Vẽ khung chữ nhật tùy chỉnh vị trí và kích thước.
  - ⬭ **Custom Ellipse Mask:** Vùng chọn hình elip/tròn.
  - 🖌️ **Freehand Brush:** Cọ vẽ tự do với thanh điều chỉnh độ dày nét cọ.
- **Video Trim Slider:** Cắt ngắn video, chọn phân đoạn trước khi xóa để tối ưu thời gian xử lý và hạn ngạch.
- **Màn hình Tiến độ thời gian thực (Real-time Progress):**
  - Hiển thị % tiến độ động (0% - 100%).
  - Đếm số frame đã xử lý (`Frame: 150 / 300`).
  - Cập nhật giai đoạn: Khởi tạo mô hình -> Bám theo mask -> Inpainting nền -> Mã hóa video kết quả.
  - Nút Hủy task (tự động hoàn tiền/credits ngay lập tức).
- **Trình phát video & Đánh giá (Player & Feedback):**
  - Xem trước video kết quả đã xóa sạch vật thể.
  - Lưu vào Camera Roll / Thư viện ảnh.
  - Gửi đánh giá 5 sao và bình luận chất lượng inpainting.
- **Hệ thống VIP & Subscription:** Bảng giá các gói Pro/VIP, quản lý Credits và mua lượt xử lý.

---

## 3. Hướng dẫn khởi chạy

### Backend Server (Spring Boot)
```bash
# Chạy kiểm thử tự động (17 bài test)
mvn clean test

# Chạy server cục bộ (Port 8080)
mvn spring-boot:run
```
- Swagger UI tài liệu API: `http://localhost:8080/swagger-ui/index.html`

### Android App (Kotlin)
```bash
cd android
./gradlew.bat compileDebugKotlin
./gradlew.bat assembleDebug
```
- Mở thư mục `android/` bằng Android Studio và nhấn Run.

### iOS App (Swift)
```bash
cd ios
# Mở dự án bằng Xcode trên máy macOS
open AIVideoEraser.xcodeproj
```
- Nhấn `Cmd + R` để chạy trên Simulator hoặc thiết bị iPhone thật.
