package com.aivideoeraser.controller;

import com.aivideoeraser.dto.ApiResponse;
import com.aivideoeraser.dto.FeedbackRequest;
import com.aivideoeraser.dto.FeedbackResponse;
import com.aivideoeraser.security.UserPrincipal;
import com.aivideoeraser.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Feedback & Quality Rating", description = "Endpoints for rating and providing feedback on AI video erasure results")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/{taskId}/feedback")
    @Operation(summary = "Submit quality rating and feedback for completed task")
    public ResponseEntity<ApiResponse<FeedbackResponse>> submitFeedback(
            @PathVariable String taskId,
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        FeedbackResponse response = feedbackService.submitFeedback(taskId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted successfully", response));
    }

    @GetMapping("/{taskId}/feedback")
    @Operation(summary = "Get submitted feedback for a task")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedback(
            @PathVariable String taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        FeedbackResponse response = feedbackService.getFeedback(taskId, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
