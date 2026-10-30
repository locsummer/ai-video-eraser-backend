package com.aivideoeraser.controller;

import com.aivideoeraser.entity.EraseTask;
import com.aivideoeraser.entity.TaskStatus;
import com.aivideoeraser.entity.Video;
import com.aivideoeraser.exception.BadRequestException;
import com.aivideoeraser.exception.ResourceNotFoundException;
import com.aivideoeraser.repository.EraseTaskRepository;
import com.aivideoeraser.repository.VideoRepository;
import com.aivideoeraser.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/files/stream")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media Streaming & Download", description = "Endpoints for streaming and downloading video files with HTTP byte-range support")
public class FileStreamController {

    private final VideoRepository videoRepository;
    private final EraseTaskRepository eraseTaskRepository;
    private final FileStorageService fileStorageService;

    @GetMapping("/video/{videoId}")
    @Operation(summary = "Stream or download uploaded source video by ID")
    public ResponseEntity<ResourceRegion> streamSourceVideo(
            @PathVariable Long videoId,
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "download", defaultValue = "false") boolean download) throws IOException {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        Path filePath = fileStorageService.getVideoPath(video.getStoredFilename());
        Resource resource = fileStorageService.loadFileAsResource(filePath);

        return buildStreamingResponse(resource, video.getOriginalFilename(), video.getMimeType(), headers, download);
    }

    @GetMapping("/result/{taskId}")
    @Operation(summary = "Stream or download rendered result video by task ID")
    public ResponseEntity<ResourceRegion> streamResultVideo(
            @PathVariable String taskId,
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "download", defaultValue = "false") boolean download) throws IOException {

        EraseTask task = eraseTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("EraseTask", "id", taskId));

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new BadRequestException("Task has not completed yet. Current status: " + task.getStatus());
        }

        if (task.getResultVideoPath() == null) {
            throw new ResourceNotFoundException("Result video file not found for task: " + taskId);
        }

        Path filePath = Paths.get(task.getResultVideoPath());
        Resource resource = fileStorageService.loadFileAsResource(filePath);

        String filename = "erased_" + task.getVideo().getOriginalFilename();
        return buildStreamingResponse(resource, filename, "video/mp4", headers, download);
    }

    private ResponseEntity<ResourceRegion> buildStreamingResponse(
            Resource resource, String filename, String mimeType, HttpHeaders headers, boolean download) throws IOException {

        long contentLength = resource.contentLength();
        HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().get(0);

        MediaType mediaType = MediaType.parseMediaType(mimeType != null ? mimeType : "video/mp4");
        ContentDisposition disposition = ContentDisposition.builder(download ? "attachment" : "inline")
                .filename(filename)
                .build();

        if (range != null) {
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1024 * 1024L, end - start + 1); // 1MB chunks for smooth streaming
            ResourceRegion region = new ResourceRegion(resource, start, rangeLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(region);
        } else {
            long rangeLength = Math.min(1024 * 1024L, contentLength);
            ResourceRegion region = new ResourceRegion(resource, 0, rangeLength);

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(region);
        }
    }
}
