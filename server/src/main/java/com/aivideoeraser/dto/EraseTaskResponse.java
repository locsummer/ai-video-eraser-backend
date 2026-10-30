package com.aivideoeraser.dto;

import com.aivideoeraser.entity.EraseMode;
import com.aivideoeraser.entity.ModelEngine;
import com.aivideoeraser.entity.QualityMode;
import com.aivideoeraser.entity.TaskStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EraseTaskResponse {
    private String id;
    private Long videoId;
    private String videoFilename;
    private EraseMode eraseMode;
    private ModelEngine modelEngine;
    private QualityMode qualityMode;
    private Boolean autoTracking;
    private Double trimStartTime;
    private Double trimEndTime;
    private TaskStatus status;
    private Integer progress;
    private Integer currentFrame;
    private Integer totalFrames;
    private String statusMessage;
    private String resultVideoUrl;
    private String downloadUrl;
    private Long resultFileSize;
    private Double resultDurationSeconds;
    private Integer creditsDeducted;
    private Boolean creditsRefunded;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
