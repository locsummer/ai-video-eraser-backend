package com.aivideoeraser.controller;

import com.aivideoeraser.dto.ApiResponse;
import com.aivideoeraser.entity.TaskStatus;
import com.aivideoeraser.repository.EraseTaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health & Monitoring", description = "Endpoints for server health and runtime monitoring")
public class HealthController {

    private final EraseTaskRepository eraseTaskRepository;

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    @GetMapping
    @Operation(summary = "System health check, storage capacity, and GPU queue status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("serverTime", LocalDateTime.now());

        // Check storage capacity
        File storagePath = new File(uploadDir);
        long freeSpaceBytes = storagePath.getFreeSpace();
        long totalSpaceBytes = storagePath.getTotalSpace();
        health.put("freeDiskSpaceGB", Math.round((freeSpaceBytes / (1024.0 * 1024 * 1024)) * 10.0) / 10.0);
        health.put("totalDiskSpaceGB", Math.round((totalSpaceBytes / (1024.0 * 1024 * 1024)) * 10.0) / 10.0);

        // Queue metrics
        health.put("queuedJobsCount", eraseTaskRepository.countByStatus(TaskStatus.QUEUED));
        health.put("processingJobsCount", eraseTaskRepository.countByStatus(TaskStatus.PROCESSING));

        return ResponseEntity.ok(ApiResponse.success("Server is healthy", health));
    }
}
