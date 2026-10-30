package com.aivideoeraser.controller;

import com.aivideoeraser.dto.*;
import com.aivideoeraser.entity.EraseTask;
import com.aivideoeraser.exception.ForbiddenException;
import com.aivideoeraser.service.EraseTaskService;
import com.aivideoeraser.worker.InpaintingWorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/worker")
@RequiredArgsConstructor
@Tag(name = "GPU Worker Integration", description = "Endpoints for external GPU workers (RunPod/Vast.ai) to fetch jobs, stream progress, and report completion")
public class WorkerController {

    private final InpaintingWorkerService workerService;
    private final EraseTaskService eraseTaskService;

    private void authenticateWorker(String workerKey) {
        if (workerKey == null || !workerService.validateWorkerKey(workerKey)) {
            throw new ForbiddenException("Invalid or missing X-Worker-Key header");
        }
    }

    @PostMapping("/jobs/next")
    @Operation(summary = "Poll next available queued task for processing by GPU worker")
    public ResponseEntity<ApiResponse<EraseTaskResponse>> getNextJob(
            @RequestHeader(value = "X-Worker-Key", required = false) String workerKey) {
        authenticateWorker(workerKey);
        EraseTask nextTask = workerService.getNextJobForWorker();
        if (nextTask == null) {
            return ResponseEntity.ok(ApiResponse.success("No jobs in queue", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Job assigned", eraseTaskService.mapToTaskResponse(nextTask)));
    }

    @PostMapping("/jobs/{taskId}/progress")
    @Operation(summary = "Report real-time task progress (frame count & percentage)")
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @PathVariable String taskId,
            @RequestHeader(value = "X-Worker-Key", required = false) String workerKey,
            @Valid @RequestBody TaskProgressUpdateDto update) {
        authenticateWorker(workerKey);
        eraseTaskService.updateProgress(taskId, update);
        return ResponseEntity.ok(ApiResponse.success("Progress updated", null));
    }

    @PostMapping("/jobs/{taskId}/complete")
    @Operation(summary = "Report task completion with final rendered video details")
    public ResponseEntity<ApiResponse<Void>> completeJob(
            @PathVariable String taskId,
            @RequestHeader(value = "X-Worker-Key", required = false) String workerKey,
            @Valid @RequestBody TaskCompleteDto complete) {
        authenticateWorker(workerKey);
        eraseTaskService.completeTask(taskId, complete);
        return ResponseEntity.ok(ApiResponse.success("Task marked as completed", null));
    }

    @PostMapping("/jobs/{taskId}/fail")
    @Operation(summary = "Report task failure (automatically refunds user credits)")
    public ResponseEntity<ApiResponse<Void>> failJob(
            @PathVariable String taskId,
            @RequestHeader(value = "X-Worker-Key", required = false) String workerKey,
            @Valid @RequestBody TaskFailDto fail) {
        authenticateWorker(workerKey);
        eraseTaskService.failTask(taskId, fail);
        return ResponseEntity.ok(ApiResponse.success("Task marked as failed and credits refunded", null));
    }
}
