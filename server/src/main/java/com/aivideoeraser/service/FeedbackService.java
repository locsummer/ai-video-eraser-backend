package com.aivideoeraser.service;

import com.aivideoeraser.dto.FeedbackRequest;
import com.aivideoeraser.dto.FeedbackResponse;
import com.aivideoeraser.entity.EraseTask;
import com.aivideoeraser.entity.Role;
import com.aivideoeraser.entity.TaskFeedback;
import com.aivideoeraser.entity.TaskStatus;
import com.aivideoeraser.entity.User;
import com.aivideoeraser.exception.BadRequestException;
import com.aivideoeraser.exception.ForbiddenException;
import com.aivideoeraser.exception.ResourceNotFoundException;
import com.aivideoeraser.repository.EraseTaskRepository;
import com.aivideoeraser.repository.TaskFeedbackRepository;
import com.aivideoeraser.repository.UserRepository;
import com.aivideoeraser.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final TaskFeedbackRepository taskFeedbackRepository;
    private final EraseTaskRepository eraseTaskRepository;
    private final UserRepository userRepository;

    @Transactional
    public FeedbackResponse submitFeedback(String taskId, FeedbackRequest request, UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        if (!task.getUser().getId().equals(user.getId()) && principal.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You can only submit feedback for your own tasks");
        }

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new BadRequestException("Feedback can only be submitted for completed tasks");
        }

        taskFeedbackRepository.findByTask(task).ifPresent(f -> {
            throw new BadRequestException("Feedback has already been submitted for this task");
        });

        TaskFeedback feedback = TaskFeedback.builder()
                .task(task)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .category(request.getCategory() != null ? request.getCategory() : "INPAINTING_QUALITY")
                .build();

        TaskFeedback saved = taskFeedbackRepository.save(feedback);
        log.info("Feedback submitted for task {}: {} stars", taskId, saved.getRating());

        return mapToFeedbackResponse(saved);
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(String taskId, UserPrincipal principal) {
        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        if (!task.getUser().getId().equals(principal.getId()) && principal.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You do not have permission to view feedback for this task");
        }

        TaskFeedback feedback = taskFeedbackRepository.findByTask(task)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found for task: " + taskId));

        return mapToFeedbackResponse(feedback);
    }

    private FeedbackResponse mapToFeedbackResponse(TaskFeedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .taskId(feedback.getTask().getId())
                .userId(feedback.getUser().getId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .category(feedback.getCategory())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
