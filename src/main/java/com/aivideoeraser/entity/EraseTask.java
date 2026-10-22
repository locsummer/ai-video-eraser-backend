package com.aivideoeraser.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "erase_tasks", indexes = {
        @Index(name = "idx_task_user", columnList = "user_id"),
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_task_video", columnList = "video_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EraseTask {

    @Id
    @Column(length = 64)
    private String id; // UUID string

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EraseMode eraseMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ModelEngine modelEngine = ModelEngine.PRO_PAINTER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private QualityMode qualityMode = QualityMode.STANDARD;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoTracking = true;

    private Double trimStartTime;

    private Double trimEndTime;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String maskDataJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    @Builder.Default
    private Integer currentFrame = 0;

    @Builder.Default
    private Integer totalFrames = 0;

    @Column(length = 500)
    private String statusMessage;

    @Column(length = 500)
    private String resultVideoPath;

    @Column(length = 500)
    private String resultVideoUrl;

    private Long resultFileSize;

    private Double resultDurationSeconds;

    @Column(nullable = false)
    @Builder.Default
    private Integer creditsDeducted = 10;

    @Column(nullable = false)
    @Builder.Default
    private Boolean creditsRefunded = false;

    @Column(length = 100)
    private String workerId;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
