package com.aivideoeraser;

import com.aivideoeraser.dto.*;
import com.aivideoeraser.entity.EraseMode;
import com.aivideoeraser.entity.ModelEngine;
import com.aivideoeraser.entity.QualityMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AiVideoEraserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static Long videoId;
    private static String taskId;

    @Test
    @Order(1)
    void testRegisterUser() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("namle@example.com")
                .password("Password@123")
                .fullName("Le Giang Nam")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("namle@example.com"))
                .andExpect(jsonPath("$.data.credits").value(50))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @Order(2)
    void testLoginUser() throws Exception {
        LoginRequest loginReq = LoginRequest.builder()
                .email("namle@example.com")
                .password("Password@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readTree(responseBody).get("data").traverse(objectMapper).readValueAs(AuthResponse.class);
        jwtToken = authResponse.getToken();
    }

    @Test
    @Order(3)
    void testGetProfile() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("namle@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Le Giang Nam"))
                .andExpect(jsonPath("$.data.credits").value(50))
                .andExpect(jsonPath("$.data.activeSubscription.planName").value("FREE"));
    }

    @Test
    @Order(4)
    void testUploadVideo() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample_beach.mp4",
                "video/mp4",
                "Dummy video binary stream content for testing purposes".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/v1/videos/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.originalFilename").value("sample_beach.mp4"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        videoId = objectMapper.readTree(responseBody).get("data").get("id").asLong();
    }

    @Test
    @Order(5)
    void testCreateEraseTask() throws Exception {
        MaskData mask = MaskData.builder()
                .shapeType("RECTANGLE")
                .boundingBox(RectDto.builder()
                        .x(120.5)
                        .y(200.0)
                        .width(150.0)
                        .height(250.0)
                        .startFrame(0)
                        .endFrame(150)
                        .build())
                .build();

        CreateEraseTaskRequest taskReq = CreateEraseTaskRequest.builder()
                .videoId(videoId)
                .eraseMode(EraseMode.PEOPLE_REMOVE)
                .modelEngine(ModelEngine.PRO_PAINTER)
                .qualityMode(QualityMode.STANDARD)
                .autoTracking(true)
                .trimConfig(TrimConfig.builder().startTimeSeconds(0.0).endTimeSeconds(10.0).build())
                .maskData(mask)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/tasks/erase")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("QUEUED"))
                .andExpect(jsonPath("$.data.eraseMode").value("PEOPLE_REMOVE"))
                .andExpect(jsonPath("$.data.creditsDeducted").value(10))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        taskId = objectMapper.readTree(responseBody).get("data").get("id").asText();
    }

    @Test
    @Order(6)
    void testGetTaskDetail() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(taskId));
    }

    @Test
    @Order(7)
    void testGetPlans() throws Exception {
        mockMvc.perform(get("/api/v1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @Order(8)
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    @Order(9)
    void testWorkerFlow() throws Exception {
        // Test Worker key authentication
        TaskProgressUpdateDto update = TaskProgressUpdateDto.builder()
                .progress(60)
                .currentFrame(180)
                .totalFrames(300)
                .statusMessage("Inpainting progress update from test")
                .build();

        mockMvc.perform(post("/api/v1/worker/jobs/" + taskId + "/progress")
                        .header("X-Worker-Key", "aivideo-eraser-internal-worker-secret-key-2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Test Complete Job
        TaskCompleteDto complete = TaskCompleteDto.builder()
                .statusMessage("Processing done successfully")
                .build();

        mockMvc.perform(post("/api/v1/worker/jobs/" + taskId + "/complete")
                        .header("X-Worker-Key", "aivideo-eraser-internal-worker-secret-key-2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complete)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(10)
    void testSubmitFeedback() throws Exception {
        FeedbackRequest feedbackReq = FeedbackRequest.builder()
                .rating(5)
                .comment("Inpainting removed tourist seamlessly!")
                .category("INPAINTING_QUALITY")
                .build();

        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/feedback")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    @Order(11)
    void testAdminEndpoints() throws Exception {
        // Login as default admin
        LoginRequest adminLogin = LoginRequest.builder()
                .email("admin@aivideoeraser.app")
                .password("Admin@123456")
                .build();

        MvcResult adminResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).get("data").get("token").asText();

        // Check Dashboard
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers", greaterThanOrEqualTo(1)));

        // Check Users list
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }
}
