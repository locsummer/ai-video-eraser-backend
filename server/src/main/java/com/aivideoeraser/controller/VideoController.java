package com.aivideoeraser.controller;

import com.aivideoeraser.dto.ApiResponse;
import com.aivideoeraser.dto.PageResponse;
import com.aivideoeraser.dto.VideoResponse;
import com.aivideoeraser.security.UserPrincipal;
import com.aivideoeraser.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Video Management", description = "Endpoints for uploading, listing, retrieving, and managing source videos")
public class VideoController {

    private final VideoService videoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload source video for object removal processing")
    public ResponseEntity<ApiResponse<VideoResponse>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        VideoResponse response = videoService.uploadVideo(file, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Video uploaded successfully", response));
    }

    @GetMapping("/{videoId}")
    @Operation(summary = "Get video metadata and playback links by ID")
    public ResponseEntity<ApiResponse<VideoResponse>> getVideoById(
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserPrincipal principal) {
        VideoResponse response = videoService.getVideoById(videoId, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get paginated list of uploaded videos for current user")
    public ResponseEntity<ApiResponse<PageResponse<VideoResponse>>> getUserVideos(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<VideoResponse> response = videoService.getUserVideos(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{videoId}")
    @Operation(summary = "Delete an uploaded video")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserPrincipal principal) {
        videoService.deleteVideo(videoId, principal);
        return ResponseEntity.ok(ApiResponse.success("Video deleted successfully", null));
    }
}
