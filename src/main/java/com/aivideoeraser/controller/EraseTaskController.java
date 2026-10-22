package com.aivideoeraser.controller;

import com.aivideoeraser.dto.ApiResponse;
import com.aivideoeraser.dto.CreateEraseTaskRequest;
import com.aivideoeraser.dto.EraseTaskResponse;
import com.aivideoeraser.dto.PageResponse;
import com.aivideoeraser.entity.EraseMode;
import com.aivideoeraser.entity.TaskStatus;
import com.aivideoeraser.security.UserPrincipal;
import com.aivideoeraser.service.EraseTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "AI Erase Tasks", description = "Endpoints for creating and tracking AI video object removal tasks")
public class EraseTaskController {

    private final EraseTaskService eraseTaskService;

    @PostMapping("/erase")
    @Operation(summary = "Create a new AI video object removal / inpainting task")
    public ResponseEntity<ApiResponse<EraseTaskResponse>> createEraseTask(
            @Valid @RequestBody CreateEraseTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        EraseTaskResponse response = eraseTaskService.createEraseTask(request, principal);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("AI Erase task submitted to processing queue", response));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get task status, real-time progress percentage, and result download URL")
    public ResponseEntity<ApiResponse<EraseTaskResponse>> getTaskById(
            @PathVariable String taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        EraseTaskResponse response = eraseTaskService.getTaskById(taskId, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get list of tasks for the current user with optional filtering")
    public ResponseEntity<ApiResponse<PageResponse<EraseTaskResponse>>> getUserTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) EraseMode mode,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<EraseTaskResponse> response = eraseTaskService.getUserTasks(principal, status, mode, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "Cancel a queued or processing task (refunds credits)")
    public ResponseEntity<ApiResponse<EraseTaskResponse>> cancelTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        EraseTaskResponse response = eraseTaskService.cancelTask(taskId, principal);
        return ResponseEntity.ok(ApiResponse.success("Task cancelled successfully", response));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete task from user history")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        eraseTaskService.deleteTask(taskId, principal);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}
