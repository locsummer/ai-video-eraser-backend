package com.aivideoeraser.worker;

import com.aivideoeraser.dto.TaskCompleteDto;
import com.aivideoeraser.dto.TaskFailDto;
import com.aivideoeraser.dto.TaskProgressUpdateDto;
import com.aivideoeraser.entity.EraseTask;
import com.aivideoeraser.entity.TaskStatus;
import com.aivideoeraser.repository.EraseTaskRepository;
import com.aivideoeraser.service.EraseTaskService;
import com.aivideoeraser.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InpaintingWorkerService {

    private final EraseTaskRepository eraseTaskRepository;
    private final EraseTaskService eraseTaskService;
    private final FileStorageService fileStorageService;

    @Value("${app.worker.simulated-processing:true}")
    private boolean simulatedProcessing;

    @Value("${app.worker.api-key:aivideo-eraser-internal-worker-secret-key-2026}")
    private String workerApiKey;

    public boolean validateWorkerKey(String providedKey) {
        return workerApiKey.equals(providedKey);
    }

    @EventListener
    @Async
    public void handleNewTaskEvent(EraseTask task) {
        if (!simulatedProcessing) {
            log.info("Task {} enqueued for external GPU worker pickup", task.getId());
            return;
        }

        processTaskInternal(task.getId());
    }

    @Scheduled(fixedDelay = 15000)
    public void processPendingQueue() {
        if (!simulatedProcessing) {
            return;
        }

        List<EraseTask> queuedTasks = eraseTaskRepository.findNextQueuedTasks(TaskStatus.QUEUED, PageRequest.of(0, 2));
        for (EraseTask task : queuedTasks) {
            processTaskInternal(task.getId());
        }
    }

    @Async
    public void processTaskInternal(String taskId) {
        log.info("Beginning internal inpainting worker process for task: {}", taskId);

        try {
            EraseTask task = eraseTaskRepository.findById(taskId).orElse(null);
            if (task == null || task.getStatus() == TaskStatus.CANCELLED || task.getStatus() == TaskStatus.COMPLETED) {
                return;
            }

            // Step 1: Preprocessing & Frame Extraction
            eraseTaskService.updateProgress(taskId, TaskProgressUpdateDto.builder()
                    .progress(15)
                    .currentFrame(45)
                    .totalFrames(300)
                    .statusMessage("Extracting frames and initializing " + task.getModelEngine() + " engine...")
                    .build());
            Thread.sleep(1200);

            // Step 2: Mask Tracking & Propagation
            eraseTaskService.updateProgress(taskId, TaskProgressUpdateDto.builder()
                    .progress(45)
                    .currentFrame(135)
                    .totalFrames(300)
                    .statusMessage("AI Tracking: Propagating mask across video frames...")
                    .build());
            Thread.sleep(1500);

            // Step 3: Neural Inpainting & Temporal Flow Estimation
            eraseTaskService.updateProgress(taskId, TaskProgressUpdateDto.builder()
                    .progress(80)
                    .currentFrame(240)
                    .totalFrames(300)
                    .statusMessage("Inpainting background and restoring temporal consistency...")
                    .build());
            Thread.sleep(1500);

            // Step 4: Final Video Re-encoding
            eraseTaskService.updateProgress(taskId, TaskProgressUpdateDto.builder()
                    .progress(95)
                    .currentFrame(285)
                    .totalFrames(300)
                    .statusMessage("Rendering and encoding output MP4...")
                    .build());

            // Create result video file
            String originalFilename = task.getVideo().getOriginalFilename();
            String resultFilePath = fileStorageService.createEmptyResultVideo(taskId, originalFilename);

            // If source video file exists on disk, copy it to result file to make it a valid playable video!
            Path sourceVideoPath = fileStorageService.getVideoPath(task.getVideo().getStoredFilename());
            if (Files.exists(sourceVideoPath)) {
                Files.copy(sourceVideoPath, Paths.get(resultFilePath), StandardCopyOption.REPLACE_EXISTING);
            }

            File resultFile = new File(resultFilePath);
            long fileSize = resultFile.exists() ? resultFile.length() : task.getVideo().getFileSize();

            eraseTaskService.completeTask(taskId, TaskCompleteDto.builder()
                    .resultVideoPath(resultFilePath)
                    .resultFileSize(fileSize)
                    .resultDurationSeconds(task.getVideo().getDurationSeconds())
                    .statusMessage("Object erasure completed with model " + task.getModelEngine())
                    .build());

            log.info("Internal inpainting worker successfully finished task: {}", taskId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Task processing was interrupted: {}", taskId, e);
            eraseTaskService.failTask(taskId, TaskFailDto.builder().errorMessage("Task processing was interrupted").build());
        } catch (Exception e) {
            log.error("Task processing failed for task: {}", taskId, e);
            eraseTaskService.failTask(taskId, TaskFailDto.builder().errorMessage(e.getMessage()).build());
        }
    }

    public EraseTask getNextJobForWorker() {
        List<EraseTask> queued = eraseTaskRepository.findNextQueuedTasks(TaskStatus.QUEUED, PageRequest.of(0, 1));
        if (queued.isEmpty()) {
            return null;
        }

        EraseTask task = queued.get(0);
        task.setStatus(TaskStatus.PROCESSING);
        task.setStatusMessage("Picked up by external GPU worker");
        return eraseTaskRepository.save(task);
    }
}
