# AI Video Eraser - iOS Client (Swift & SwiftUI)

Ứng dụng **AI Video Eraser** cho nền tảng iOS (tương thích iPhone & iPad, iOS 16.0+), được viết hoàn toàn bằng **Swift** và **SwiftUI**.

## 1. Tính năng nổi bật (Features)
- **PhotosPicker:** Chọn video trực tiếp từ thư viện ảnh Photos với dung lượng lên đến 500MB.
- **Trình chỉnh sửa Mask AI tương tác (Interactive Canvas Overlay):**
  - **AI Object Tracking (Track AI):** Chạm để khóa đối tượng di chuyển qua các frame.
  - **Custom Rectangle & Ellipse:** Vẽ vùng chọn hình chữ nhật hoặc elip cố định.
  - **Freehand Brush:** Cọ vẽ tự do với thanh trượt điều chỉnh kích thước brush.
- **Video Trim Slider:** Cắt ngắn phân đoạn video trước khi xử lý (tương thích hạn mức Free 15s / Pro 60s / VIP 300s).
- **Màn hình Tiến độ thời gian thực (Live Progress):**
  - Vòng tròn % tiến độ động (0% - 100%).
  - Đếm frame xử lý: `Frame: 180 / 300`.
  - Hiển thị từng giai đoạn AI: *Extracting frames -> AI Tracking -> Background Inpainting -> Re-encoding*.
  - Nút **Cancel** hoàn tiền/credits tự động.
- **Xem kết quả & Đánh giá (Result & Feedback):**
  - Phát video đã được xóa sạch vật thể bằng AVPlayer.
  - Lưu vào Camera Roll / Photos Library.
  - Đánh giá sao (1 - 5 sao) phản hồi chất lượng inpainting.
- **VIP Paywall:** Bảng giá các gói Pro/VIP và quyền lợi xuất video 4K, không watermark.

## 2. Cấu trúc thư mục
```
ios/
├── AIVideoEraser.xcodeproj/      # File dự án Xcode
└── AIVideoEraser/
    ├── AIVideoEraserApp.swift     # Điểm khởi chạy ứng dụng
    ├── Models/                    # DTOs: Auth, Video, Task, Plan, Feedback
    ├── Services/                  # NetworkManager, APIEndpoint (Async/Await)
    ├── ViewModels/                # AuthViewModel, EditorViewModel, TaskViewModel
    ├── Views/                     # HomeView, EditorView, ProcessingView, ResultView, SubscriptionView, AuthView
    │   └── Components/            # VideoPlayerView, MaskCanvasView, VideoTrimSlider
    ├── Theme/                     # AppTheme màu sắc giao diện Dark theme
    └── Info.plist                 # Quyền Photo Library & Network Security
```

## 3. Hướng dẫn chạy trên Xcode
1. Mở file `AIVideoEraser.xcodeproj` bằng Xcode trên máy macOS.
2. Cấu hình địa chỉ Backend Server trong `Services/APIEndpoint.swift`:
   - Simulator: `http://localhost:8080/api/v1`
   - Thiết bị thật: `http://<IP_MAY_TINH>:8080/api/v1`
3. Chọn thiết bị giả lập (iPhone 15 / 16) và nhấn **Cmd + R** để chạy.
