package com.aivideoeraser.service;

import com.aivideoeraser.dto.PageResponse;
import com.aivideoeraser.dto.VideoResponse;
import com.aivideoeraser.entity.Role;
import com.aivideoeraser.entity.User;
import com.aivideoeraser.entity.Video;
import com.aivideoeraser.entity.VideoStatus;
import com.aivideoeraser.exception.ForbiddenException;
import com.aivideoeraser.exception.ResourceNotFoundException;
import com.aivideoeraser.repository.UserRepository;
import com.aivideoeraser.repository.VideoRepository;
import com.aivideoeraser.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public VideoResponse uploadVideo(MultipartFile file, UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        String storedFilename = fileStorageService.storeVideoFile(file, user.getId());
        Path filePath = fileStorageService.getVideoPath(storedFilename);

        // Populate video metadata
        Video video = Video.builder()
                .user(user)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .mimeType(file.getContentType() != null ? file.getContentType() : "video/mp4")
                .durationSeconds(10.0) // Default estimated duration, updated if worker probes
                .width(1920)
                .height(1080)
                .fps(30.0)
                .status(VideoStatus.READY)
                .build();

        Video savedVideo = videoRepository.save(video);
        log.info("Uploaded video id: {} for user: {}", savedVideo.getId(), user.getEmail());

        return mapToVideoResponse(savedVideo);
    }

    @Transactional(readOnly = true)
    public VideoResponse getVideoById(Long id, UserPrincipal principal) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", id));

        // Check ownership unless admin
        if (!video.getUser().getId().equals(principal.getId()) && principal.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You do not have permission to access this video");
        }

        return mapToVideoResponse(video);
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> getUserVideos(UserPrincipal principal, Pageable pageable) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Page<Video> videoPage = videoRepository.findByUserAndStatus(user, VideoStatus.READY, pageable);
        return PageResponse.from(videoPage.map(this::mapToVideoResponse));
    }

    @Transactional
    public void deleteVideo(Long id, UserPrincipal principal) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", id));

        if (!video.getUser().getId().equals(principal.getId()) && principal.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You do not have permission to delete this video");
        }

        video.setStatus(VideoStatus.DELETED);
        videoRepository.save(video);

        // Delete physical file
        fileStorageService.deleteFile(fileStorageService.getVideoPath(video.getStoredFilename()));
        log.info("Deleted video id: {}", id);
    }

    public VideoResponse mapToVideoResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .originalFilename(video.getOriginalFilename())
                .fileSize(video.getFileSize())
                .mimeType(video.getMimeType())
                .durationSeconds(video.getDurationSeconds())
                .width(video.getWidth())
                .height(video.getHeight())
                .fps(video.getFps())
                .streamUrl("/api/v1/files/stream/video/" + video.getId())
                .downloadUrl("/api/v1/files/stream/video/" + video.getId() + "?download=true")
                .thumbnailUrl(video.getThumbnailPath() != null ? "/api/v1/files/stream/thumbnail/" + video.getId() : null)
                .status(video.getStatus())
                .createdAt(video.getCreatedAt())
                .build();
    }
}
