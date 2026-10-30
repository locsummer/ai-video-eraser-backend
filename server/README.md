# AI Video Eraser - Backend Server (Spring Boot)

Backend Server hoàn chỉnh và sẵn sàng vận hành cho ứng dụng **AI Video Eraser** (hỗ trợ iOS App [id6744909217](https://apps.apple.com/us/app/ai-video-eraser/id6744909217?l=vi), Android App và Web Dashboard).

---

## 1. Công nghệ & Kiến trúc
- **Framework & Runtime:** Java 21 LTS, Spring Boot 3.3.5
- **Security:** Spring Security 6, JWT (Access Token & Refresh Token), BCrypt Password Encoder
- **Database & ORM:** Spring Data JPA, Hibernate, H2 Database (Dev), PostgreSQL (Production)
- **Video Streaming:** HTTP 206 Partial Content (Byte Range) cho AVPlayer (iOS) và ExoPlayer (Android)
- **API Documentation:** SpringDoc OpenAPI 3 / Swagger UI (`/swagger-ui/index.html`)
- **Queue & Worker:** Bất đồng bộ (`@Async`, Scheduled, Thread Pool) + Webhook / API cho External GPU Workers (RunPod / Vast.ai)
- **Containerization:** Dockerfile (Eclipse Temurin 21) & Docker Compose (PostgreSQL 16)

---

## 2. Cấu trúc thư mục Server

```
server/
├── src/main/java/com/aivideoeraser/
│   ├── AiVideoEraserApplication.java     # Entry point chính của Spring Boot
│   ├── config/                           # SecurityConfig, OpenApiConfig, DataInitializer
│   ├── controller/                       # Auth, Video, Task, Worker, Subscription, Feedback, Admin, Health
│   ├── service/                          # Logic nghiệp vụ, credits, validation, storage
│   ├── repository/                       # Spring Data JPA Repositories
│   ├── entity/                           # JPA Database Entities & Enums
│   ├── dto/                              # Request, Response, Page & Error DTOs
│   ├── security/                         # JWT Token Provider, Principal & UserDetailsService
│   ├── worker/                           # InpaintingWorkerService (Queue & GPU Worker API)
│   └── exception/                        # GlobalExceptionHandler
├── src/main/resources/
│   ├── application.yml                   # Cấu hình môi trường dev (H2 file DB)
│   └── application-prod.yml              # Cấu hình môi trường production (PostgreSQL)
├── src/test/java/com/aivideoeraser/      # 17 bài Unit & Integration tests
├── pom.xml                               # Quản lý dependencies Maven
└── Dockerfile                            # Dockerfile build server
```

---

## 3. Hướng dẫn chạy Server

### Chạy trực tiếp qua Maven:
```bash
cd server
mvn clean test
mvn spring-boot:run
```
- Swagger UI Interactive Docs: `http://localhost:8080/swagger-ui/index.html`
- Health Check: `http://localhost:8080/api/v1/health`
- H2 Console: `http://localhost:8080/h2-console`

### Chạy bằng Docker Compose (PostgreSQL):
```bash
docker-compose up --build -d
```

### Tài khoản mặc định:
- **Admin:** `admin@aivideoeraser.app` / `Admin@123456` (ROLE_ADMIN, 99,999 credits)
- **VIP Demo:** `demo@aivideoeraser.app` / `Demo@123456` (ROLE_PREMIUM, 500 credits)
