package com.aivideoeraser.service;

import com.aivideoeraser.dto.*;
import com.aivideoeraser.entity.*;
import com.aivideoeraser.exception.ResourceNotFoundException;
import com.aivideoeraser.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final EraseTaskRepository eraseTaskRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final EraseTaskService eraseTaskService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboard() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long totalVideos = videoRepository.count();
        long totalTasks = eraseTaskRepository.count();
        long completedTasks = eraseTaskRepository.countByStatus(TaskStatus.COMPLETED);
        long queuedTasks = eraseTaskRepository.countByStatus(TaskStatus.QUEUED);
        long processingTasks = eraseTaskRepository.countByStatus(TaskStatus.PROCESSING);
        long failedTasks = eraseTaskRepository.countByStatus(TaskStatus.FAILED);
        long activeSubs = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);

        double successRate = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100.0 : 0.0;

        Map<String, Long> tasksByMode = new HashMap<>();
        for (EraseMode mode : EraseMode.values()) {
            tasksByMode.put(mode.name(), 0L);
        }

        return AdminDashboardDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalVideos(totalVideos)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .queuedTasks(queuedTasks)
                .processingTasks(processingTasks)
                .failedTasks(failedTasks)
                .activeSubscriptions(activeSubs)
                .tasksByEraseMode(tasksByMode)
                .successRatePercentage(Math.round(successRate * 10.0) / 10.0)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserManagementDto> getUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return PageResponse.from(users.map(u -> UserManagementDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole())
                .status(u.getStatus())
                .credits(u.getCredits())
                .totalVideosUploaded(videoRepository.countByUser(u))
                .totalTasksSubmitted(eraseTaskRepository.countByUser(u))
                .createdAt(u.getCreatedAt())
                .build()));
    }

    @Transactional
    public UserManagementDto updateUserStatus(Long userId, UpdateUserStatusDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setStatus(dto.getStatus());
        User saved = userRepository.save(user);
        log.info("Admin updated user status for {}: {}", user.getEmail(), dto.getStatus());

        return UserManagementDto.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .role(saved.getRole())
                .status(saved.getStatus())
                .credits(saved.getCredits())
                .totalVideosUploaded(videoRepository.countByUser(saved))
                .totalTasksSubmitted(eraseTaskRepository.countByUser(saved))
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public UserManagementDto addCredits(Long userId, AddCreditsDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setCredits(user.getCredits() + dto.getCredits());
        User saved = userRepository.save(user);
        log.info("Admin added {} credits to user: {}. New total: {}", dto.getCredits(), user.getEmail(), saved.getCredits());

        return UserManagementDto.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .role(saved.getRole())
                .status(saved.getStatus())
                .credits(saved.getCredits())
                .totalVideosUploaded(videoRepository.countByUser(saved))
                .totalTasksSubmitted(eraseTaskRepository.countByUser(saved))
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<EraseTaskResponse> getAllTasks(TaskStatus status, Pageable pageable) {
        Page<EraseTask> tasks;
        if (status != null) {
            tasks = eraseTaskRepository.findByStatus(status, pageable);
        } else {
            tasks = eraseTaskRepository.findAll(pageable);
        }
        return PageResponse.from(tasks.map(eraseTaskService::mapToTaskResponse));
    }

    @Transactional
    public EraseTaskResponse requeueTask(String taskId) {
        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        task.setStatus(TaskStatus.QUEUED);
        task.setProgress(0);
        task.setStatusMessage("Task re-queued by administrator");
        task.setErrorMessage(null);
        EraseTask saved = eraseTaskRepository.save(task);

        eventPublisher.publishEvent(saved);
        log.info("Re-queued task: {}", taskId);

        return eraseTaskService.mapToTaskResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SystemSetting> getSystemSettings() {
        return systemSettingRepository.findAll();
    }

    @Transactional
    public SystemSetting updateSystemSetting(SystemSettingDto dto) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(dto.getSettingKey())
                .orElse(SystemSetting.builder().settingKey(dto.getSettingKey()).build());

        setting.setSettingValue(dto.getSettingValue());
        if (dto.getDescription() != null) {
            setting.setDescription(dto.getDescription());
        }

        return systemSettingRepository.save(setting);
    }
}
