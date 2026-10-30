package com.aivideoeraser.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskProgressUpdateDto {
    @Min(0)
    @Max(100)
    private int progress;
    private int currentFrame;
    private int totalFrames;
    private String statusMessage;
}
