package com.aivideoeraser.service;

import com.aivideoeraser.dto.*;
import com.aivideoeraser.entity.*;
import com.aivideoeraser.exception.BadRequestException;
import com.aivideoeraser.exception.ForbiddenException;
import com.aivideoeraser.exception.InsufficientCreditsException;
import com.aivideoeraser.exception.ResourceNotFoundException;
import com.aivideoeraser.repository.EraseTaskRepository;
import com.aivideoeraser.repository.SubscriptionRepository;
import com.aivideoeraser.repository.UserRepository;
import com.aivideoeraser.repository.VideoRepository;
import com.aivideoeraser.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EraseTaskService {

    private final EraseTaskRepository eraseTaskRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.credits.erase-task-credit-cost:10}")
    private int eraseTaskCreditCost;

    @Transactional
    public EraseTaskResponse createEraseTask(CreateEraseTaskRequest request, UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Video video = videoRepository.findById(request.getVideoId())
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", request.getVideoId()));

        if (!video.getUser().getId().equals(user.getId()) && principal.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You can only create erase tasks for your own uploaded videos");
        }

        // Check active subscription limits
        Subscription activeSub = subscriptionRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, SubscriptionStatus.ACTIVE)
                .orElse(null);

        int maxAllowedDuration = 15; // default free limit
        if (activeSub != null) {
            maxAllowedDuration = activeSub.getMaxDurationSeconds();
        }

        // Validate trim duration
        double taskDuration = video.getDurationSeconds() != null ? video.getDurationSeconds() : 10.0;
        double trimStart = 0.0;
        double trimEnd = taskDuration;

        if (request.getTrimConfig() != null) {
            if (request.getTrimConfig().getStartTimeSeconds() != null) {
                trimStart = request.getTrimConfig().getStartTimeSeconds();
            }
            if (request.getTrimConfig().getEndTimeSeconds() != null) {
                trimEnd = request.getTrimConfig().getEndTimeSeconds();
            }
            if (trimEnd <= trimStart) {
                throw new BadRequestException("Trim end time must be greater than start time");
            }
            taskDuration = trimEnd - trimStart;
        }

        if (taskDuration > maxAllowedDuration) {
            throw new BadRequestException(String.format("Requested video processing duration (%.1fs) exceeds your plan limit (%ds). Please trim the clip or upgrade your plan.", taskDuration, maxAllowedDuration));
        }

        // Check and deduct credits (VIP may be exempt or discounted)
        boolean isVip = activeSub != null && activeSub.getTier() == SubscriptionTier.VIP;
        int creditsToDeduct = isVip ? 0 : eraseTaskCreditCost;

        if (creditsToDeduct > 0) {
            if (user.getCredits() < creditsToDeduct) {
                throw new InsufficientCreditsException(creditsToDeduct, user.getCredits());
            }
            user.setCredits(user.getCredits() - creditsToDeduct);
            userRepository.save(user);
            log.info("Deducted {} credits from user: {}. Remaining: {}", creditsToDeduct, user.getEmail(), user.getCredits());
        }

        // Serialize mask data
        String maskDataJson = null;
        if (request.getMaskData() != null) {
            try {
                maskDataJson = objectMapper.writeValueAsString(request.getMaskData());
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Failed to serialize mask data: " + e.getMessage());
            }
        }

        String taskId = "task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        EraseTask task = EraseTask.builder()
                .id(taskId)
                .user(user)
                .video(video)
                .eraseMode(request.getEraseMode())
                .modelEngine(request.getModelEngine() != null ? request.getModelEngine() : ModelEngine.PRO_PAINTER)
                .qualityMode(request.getQualityMode() != null ? request.getQualityMode() : QualityMode.STANDARD)
                .autoTracking(request.getAutoTracking() != null ? request.getAutoTracking() : true)
                .trimStartTime(request.getTrimConfig() != null ? trimStart : null)
                .trimEndTime(request.getTrimConfig() != null ? trimEnd : null)
                .maskDataJson(maskDataJson)
                .status(TaskStatus.QUEUED)
                .progress(0)
                .statusMessage("Task queued and waiting for GPU worker")
                .creditsDeducted(creditsToDeduct)
                .creditsRefunded(false)
                .build();

        EraseTask savedTask = eraseTaskRepository.save(task);
        log.info("Created EraseTask id: {} for video id: {}, user: {}", taskId, video.getId(), user.getEmail());

        // Publish event for internal asynchronous queue processing
        eventPublisher.publishEvent(savedTask);

        return mapToTaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public EraseTaskResponse getTaskById(String taskId, UserPrincipal principal) {
        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        if (!task.getUser().getId().equals(principal.getId()) && principal.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You do not have permission to access this task");
        }

        return mapToTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public PageResponse<EraseTaskResponse> getUserTasks(UserPrincipal principal, TaskStatus status, EraseMode mode, Pageable pageable) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Page<EraseTask> taskPage;
        if (status != null) {
            taskPage = eraseTaskRepository.findByUserAndStatus(user, status, pageable);
        } else if (mode != null) {
            taskPage = eraseTaskRepository.findByUserAndEraseMode(user, mode, pageable);
        } else {
            taskPage = eraseTaskRepository.findByUser(user, pageable);
        }

        return PageResponse.from(taskPage.map(this::mapToTaskResponse));
    }

    @Transactional
    public EraseTaskResponse cancelTask(String taskId, UserPrincipal principal) {
        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        if (!task.getUser().getId().equals(principal.getId()) && principal.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You do not have permission to cancel this task");
        }

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel an already completed task");
        }

        if (task.getStatus() == TaskStatus.CANCELLED) {
            return mapToTaskResponse(task);
        }

        task.setStatus(TaskStatus.CANCELLED);
        task.setStatusMessage("Task cancelled by user");

        // Refund credits if not already refunded
        if (!task.getCreditsRefunded() && task.getCreditsDeducted() > 0) {
            User user = task.getUser();
            user.setCredits(user.getCredits() + task.getCreditsDeducted());
            userRepository.save(user);
            task.setCreditsRefunded(true);
            log.info("Refunded {} credits to user: {} due to cancellation of task: {}", task.getCreditsDeducted(), user.getEmail(), taskId);
        }

        EraseTask saved = eraseTaskRepository.save(task);
        return mapToTaskResponse(saved);
    }

    @Transactional
    public void deleteTask(String taskId, UserPrincipal principal) {
        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        if (!task.getUser().getId().equals(principal.getId()) && principal.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You do not have permission to delete this task");
        }

        eraseTaskRepository.delete(task);
        log.info("Deleted task: {}", taskId);
    }

    @Transactional
    public void updateProgress(String taskId, TaskProgressUpdateDto update) {
        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        task.setStatus(TaskStatus.PROCESSING);
        task.setProgress(update.getProgress());
        task.setCurrentFrame(update.getCurrentFrame());
        task.setTotalFrames(update.getTotalFrames());
        if (update.getStatusMessage() != null) {
            task.setStatusMessage(update.getStatusMessage());
        }
        if (task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        }

        eraseTaskRepository.save(task);
    }

    @Transactional
    public void completeTask(String taskId, TaskCompleteDto complete) {
        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        task.setStatus(TaskStatus.COMPLETED);
        task.setProgress(100);
        task.setStatusMessage(complete.getStatusMessage() != null ? complete.getStatusMessage() : "Video object erasure completed successfully");
        task.setResultVideoPath(complete.getResultVideoPath());
        task.setResultVideoUrl("/api/v1/files/stream/result/" + task.getId());
        task.setResultFileSize(complete.getResultFileSize() != null ? complete.getResultFileSize() : task.getVideo().getFileSize());
        task.setResultDurationSeconds(complete.getResultDurationSeconds() != null ? complete.getResultDurationSeconds() : task.getVideo().getDurationSeconds());
        task.setCompletedAt(LocalDateTime.now());

        eraseTaskRepository.save(task);
        log.info("Completed EraseTask: {}", taskId);
    }

    @Transactional
    public void failTask(String taskId, TaskFailDto fail) {
        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        task.setStatus(TaskStatus.FAILED);
        task.setErrorMessage(fail.getErrorMessage());
        task.setStatusMessage("Failed: " + fail.getErrorMessage());

        // Refund credits automatically on failure!
        if (!task.getCreditsRefunded() && task.getCreditsDeducted() > 0) {
            User user = task.getUser();
            user.setCredits(user.getCredits() + task.getCreditsDeducted());
            userRepository.save(user);
            task.setCreditsRefunded(true);
            log.info("Automatically refunded {} credits to user: {} due to task failure: {}", task.getCreditsDeducted(), user.getEmail(), taskId);
        }

        eraseTaskRepository.save(task);
        log.warn("EraseTask {} failed with error: {}", taskId, fail.getErrorMessage());
    }

    public EraseTaskResponse mapToTaskResponse(EraseTask task) {
        return EraseTaskResponse.builder()
                .id(task.getId())
                .videoId(task.getVideo() != null ? task.getVideo().getId() : null)
                .videoFilename(task.getVideo() != null ? task.getVideo().getOriginalFilename() : null)
                .eraseMode(task.getEraseMode())
                .modelEngine(task.getModelEngine())
                .qualityMode(task.getQualityMode())
                .autoTracking(task.getAutoTracking())
                .trimStartTime(task.getTrimStartTime())
                .trimEndTime(task.getTrimEndTime())
                .status(task.getStatus())
                .progress(task.getProgress())
                .currentFrame(task.getCurrentFrame())
                .totalFrames(task.getTotalFrames())
                .statusMessage(task.getStatusMessage())
                .resultVideoUrl(task.getResultVideoUrl())
                .downloadUrl(task.getStatus() == TaskStatus.COMPLETED ? "/api/v1/files/stream/result/" + task.getId() + "?download=true" : null)
                .resultFileSize(task.getResultFileSize())
                .resultDurationSeconds(task.getResultDurationSeconds())
                .creditsDeducted(task.getCreditsDeducted())
                .creditsRefunded(task.getCreditsRefunded())
                .errorMessage(task.getErrorMessage())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
