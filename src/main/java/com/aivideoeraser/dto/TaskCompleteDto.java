package com.aivideoeraser.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompleteDto {
    private String resultVideoPath;
    private String resultVideoUrl;
    private Long resultFileSize;
    private Double resultDurationSeconds;
    private String statusMessage;
}
