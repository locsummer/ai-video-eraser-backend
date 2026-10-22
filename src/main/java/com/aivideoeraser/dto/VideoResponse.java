package com.aivideoeraser.dto;

import com.aivideoeraser.entity.VideoStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private Long id;
    private String originalFilename;
    private Long fileSize;
    private String mimeType;
    private Double durationSeconds;
    private Integer width;
    private Integer height;
    private Double fps;
    private String streamUrl;
    private String downloadUrl;
    private String thumbnailUrl;
    private VideoStatus status;
    private LocalDateTime createdAt;
}
