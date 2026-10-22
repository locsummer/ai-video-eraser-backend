package com.aivideoeraser;

import com.aivideoeraser.dto.CreateEraseTaskRequest;
import com.aivideoeraser.dto.EraseTaskResponse;
import com.aivideoeraser.dto.MaskData;
import com.aivideoeraser.dto.RectDto;
import com.aivideoeraser.entity.*;
import com.aivideoeraser.exception.InsufficientCreditsException;
import com.aivideoeraser.repository.EraseTaskRepository;
import com.aivideoeraser.repository.SubscriptionRepository;
import com.aivideoeraser.repository.UserRepository;
import com.aivideoeraser.repository.VideoRepository;
import com.aivideoeraser.security.UserPrincipal;
import com.aivideoeraser.service.EraseTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EraseTaskServiceTest {

    @Mock
    private EraseTaskRepository eraseTaskRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EraseTaskService eraseTaskService;

    private User sampleUser;
    private Video sampleVideo;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eraseTaskService, "eraseTaskCreditCost", 10);

        sampleUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .fullName("Regular User")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .credits(50)
                .build();

        userPrincipal = UserPrincipal.create(sampleUser);

        sampleVideo = Video.builder()
                .id(100L)
                .user(sampleUser)
                .originalFilename("test_clip.mp4")
                .storedFilename("u1_abc123.mp4")
                .filePath("/tmp/video.mp4")
                .fileSize(1024000L)
                .durationSeconds(12.0)
                .status(VideoStatus.READY)
                .build();
    }

    @Test
    void testCreateEraseTask_Success() {
        CreateEraseTaskRequest req = CreateEraseTaskRequest.builder()
                .videoId(100L)
                .eraseMode(EraseMode.OBJECT_REMOVE)
                .modelEngine(ModelEngine.PRO_PAINTER)
                .qualityMode(QualityMode.STANDARD)
                .autoTracking(true)
                .maskData(MaskData.builder()
                        .boundingBox(RectDto.builder().x(100).y(150).width(200).height(300).build())
                        .build())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(videoRepository.findById(100L)).thenReturn(Optional.of(sampleVideo));
        when(subscriptionRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(sampleUser, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(eraseTaskRepository.save(any(EraseTask.class))).thenAnswer(invocation -> {
            EraseTask saved = invocation.getArgument(0);
            return saved;
        });

        EraseTaskResponse resp = eraseTaskService.createEraseTask(req, userPrincipal);

        assertNotNull(resp);
        assertEquals(TaskStatus.QUEUED, resp.getStatus());
        assertEquals(10, resp.getCreditsDeducted());
        assertEquals(40, sampleUser.getCredits()); // 50 - 10 = 40
        verify(userRepository, times(1)).save(sampleUser);
        verify(eraseTaskRepository, times(1)).save(any(EraseTask.class));
    }

    @Test
    void testCreateEraseTask_InsufficientCredits_ThrowsException() {
        sampleUser.setCredits(5); // Only 5 credits, requires 10
        CreateEraseTaskRequest req = CreateEraseTaskRequest.builder()
                .videoId(100L)
                .eraseMode(EraseMode.TEXT_WATERMARK_REMOVE)
                .maskData(MaskData.builder().build())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(videoRepository.findById(100L)).thenReturn(Optional.of(sampleVideo));
        when(subscriptionRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(sampleUser, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientCreditsException.class, () -> eraseTaskService.createEraseTask(req, userPrincipal));
        verify(eraseTaskRepository, never()).save(any(EraseTask.class));
    }

    @Test
    void testCancelTask_RefundsCredits() {
        EraseTask task = EraseTask.builder()
                .id("task_12345")
                .user(sampleUser)
                .video(sampleVideo)
                .status(TaskStatus.QUEUED)
                .creditsDeducted(10)
                .creditsRefunded(false)
                .build();

        when(eraseTaskRepository.findById("task_12345")).thenReturn(Optional.of(task));
        when(eraseTaskRepository.save(any(EraseTask.class))).thenAnswer(i -> i.getArgument(0));

        EraseTaskResponse resp = eraseTaskService.cancelTask("task_12345", userPrincipal);

        assertEquals(TaskStatus.CANCELLED, resp.getStatus());
        assertTrue(task.getCreditsRefunded());
        assertEquals(60, sampleUser.getCredits()); // 50 + 10 refunded = 60
        verify(userRepository, times(1)).save(sampleUser);
    }
}
