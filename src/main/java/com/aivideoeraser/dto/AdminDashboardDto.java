package com.aivideoeraser.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDto {
    private long totalUsers;
    private long activeUsers;
    private long totalVideos;
    private long totalTasks;
    private long completedTasks;
    private long queuedTasks;
    private long processingTasks;
    private long failedTasks;
    private long activeSubscriptions;
    private Map<String, Long> tasksByEraseMode;
    private double successRatePercentage;
}
