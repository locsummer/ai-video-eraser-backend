# AI Video Eraser - Android Client (Kotlin & Jetpack Compose)

Ứng dụng **AI Video Eraser** cho nền tảng Android (hỗ trợ Android 8.0 Oreo đến Android 14+), được phát triển 100% bằng **Kotlin**, **Jetpack Compose (Material 3)** và kiến trúc **Clean Architecture / MVVM**.

## 1. Công nghệ & Thư viện sử dụng
- **UI:** Jetpack Compose (BOM 2024.05.00), Material 3.
- **Trình phát video:** AndroidX Media3 (ExoPlayer 1.3.1) hỗ trợ HTTP Byte-Range Streaming.
- **Mạng:** Retrofit 2.11.0, OkHttp 3 Logging Interceptor, Coroutines, StateFlow.
- **Canvas cử chỉ:** Hỗ trợ vẽ Rectangle, Ellipse, Freehand Brush và Tap-to-Track point target.

## 2. Cấu trúc thư mục
```
android/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/aivideoeraser/android/
│           ├── MainActivity.kt
│           ├── AiVideoEraserApp.kt
│           ├── data/
│           │   ├── api/          # RetrofitClient & ApiService
│           │   ├── model/        # Request & Response DTOs
│           │   └── repository/   # AuthRepository, VideoRepository, TaskRepository
│           ├── ui/
│           │   ├── theme/        # DarkTheme, Colors, Typography
│           │   ├── components/   # VideoPlayerView, MaskCanvasOverlay, VideoTrimSlider
│           │   ├── screens/      # HomeScreen, EditorScreen, ProcessingScreen, ResultScreen, SubscriptionScreen, AuthScreen
│           │   └── navigation/   # NavGraph & Compose Routes
│           └── viewmodel/        # AuthViewModel, EditorViewModel, TaskViewModel
├── build.gradle.kts
└── settings.gradle.kts
```

## 3. Hướng dẫn biên dịch & chạy
Biên dịch ứng dụng bằng Gradle Wrapper:
```bash
cd android
./gradlew.bat assembleDebug
```
File APK sẽ được tạo tại `android/app/build/outputs/apk/debug/app-debug.apk`.

Địa chỉ Backend mặc định cho Android Emulator là `http://10.0.2.2:8080/api/v1/`. Khi chạy trên điện thoại thật qua cùng mạng Wi-Fi, đổi thành IP máy tính của bạn trong `RetrofitClient.kt`.
