package com.aivideoeraser.controller;

import com.aivideoeraser.dto.*;
import com.aivideoeraser.entity.SystemSetting;
import com.aivideoeraser.entity.TaskStatus;
import com.aivideoeraser.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin Management & Dashboard", description = "Endpoints for administrators to monitor system performance, manage users, and configure parameters")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard statistics (users, tasks, active subscriptions, GPU queue status)")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboard() {
        AdminDashboardDto dashboard = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/users")
    @Operation(summary = "Get list of registered users with activity stats")
    public ResponseEntity<ApiResponse<PageResponse<UserManagementDto>>> getUsers(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<UserManagementDto> users = adminService.getUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "Update user account status (ACTIVE, SUSPENDED, LOCKED)")
    public ResponseEntity<ApiResponse<UserManagementDto>> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusDto dto) {
        UserManagementDto user = adminService.updateUserStatus(userId, dto);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", user));
    }

    @PostMapping("/users/{userId}/credits")
    @Operation(summary = "Add or grant credits to a user account")
    public ResponseEntity<ApiResponse<UserManagementDto>> addCredits(
            @PathVariable Long userId,
            @Valid @RequestBody AddCreditsDto dto) {
        UserManagementDto user = adminService.addCredits(userId, dto);
        return ResponseEntity.ok(ApiResponse.success("Credits added successfully", user));
    }

    @GetMapping("/tasks")
    @Operation(summary = "View all system tasks with optional status filtering")
    public ResponseEntity<ApiResponse<PageResponse<EraseTaskResponse>>> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<EraseTaskResponse> tasks = adminService.getAllTasks(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @PostMapping("/tasks/{taskId}/requeue")
    @Operation(summary = "Re-queue a failed task for reprocessing")
    public ResponseEntity<ApiResponse<EraseTaskResponse>> requeueTask(@PathVariable String taskId) {
        EraseTaskResponse task = adminService.requeueTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task successfully re-queued", task));
    }

    @GetMapping("/system-settings")
    @Operation(summary = "Get system configurations")
    public ResponseEntity<ApiResponse<List<SystemSetting>>> getSystemSettings() {
        List<SystemSetting> settings = adminService.getSystemSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/system-settings")
    @Operation(summary = "Update system configuration setting")
    public ResponseEntity<ApiResponse<SystemSetting>> updateSystemSetting(@Valid @RequestBody SystemSettingDto dto) {
        SystemSetting setting = adminService.updateSystemSetting(dto);
        return ResponseEntity.ok(ApiResponse.success("System setting updated successfully", setting));
    }
}
