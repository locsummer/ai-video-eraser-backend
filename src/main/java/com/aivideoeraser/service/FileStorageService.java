package com.aivideoeraser.service;

import com.aivideoeraser.exception.BadRequestException;
import com.aivideoeraser.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.storage.upload-dir}")
    private String uploadDir;

    @Value("${app.storage.video-dir}")
    private String videoDir;

    @Value("${app.storage.result-dir}")
    private String resultDir;

    @Value("${app.storage.thumbnail-dir}")
    private String thumbnailDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("mp4", "mov", "avi", "mkv", "webm", "m4v");

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            Files.createDirectories(Paths.get(videoDir));
            Files.createDirectories(Paths.get(resultDir));
            Files.createDirectories(Paths.get(thumbnailDir));
            log.info("Initialized storage directories successfully");
        } catch (IOException e) {
            log.error("Could not create upload directories", e);
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    public String storeVideoFile(MultipartFile file, Long userId) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "video.mp4");

        // Validate extension
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported video format. Allowed formats: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Generate unique stored file name
        String storedFilename = "u" + userId + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path targetLocation = Paths.get(videoDir).resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored video file: {} -> {}", originalFilename, targetLocation.toAbsolutePath());
            return storedFilename;
        } catch (IOException e) {
            log.error("Failed to store file: " + originalFilename, e);
            throw new RuntimeException("Failed to store video file", e);
        }
    }

    public String createEmptyResultVideo(String taskId, String originalFilename) {
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (extension.isEmpty()) extension = "mp4";
        String resultFilename = "result_" + taskId + "." + extension;
        Path targetLocation = Paths.get(resultDir).resolve(resultFilename);

        try {
            if (!Files.exists(targetLocation)) {
                // If original video exists, copy or create result placeholder
                Files.createFile(targetLocation);
            }
            return targetLocation.toString();
        } catch (IOException e) {
            log.error("Failed to prepare result video file for task: " + taskId, e);
            throw new RuntimeException("Failed to prepare result video file", e);
        }
    }

    public Path getVideoPath(String storedFilename) {
        return Paths.get(videoDir).resolve(storedFilename).normalize();
    }

    public Path getResultPath(String resultFilename) {
        return Paths.get(resultDir).resolve(resultFilename).normalize();
    }

    public Resource loadFileAsResource(Path filePath) {
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or not readable: " + filePath.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File path malformed: " + filePath.getFileName());
        }
    }

    public void deleteFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", filePath, e);
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
