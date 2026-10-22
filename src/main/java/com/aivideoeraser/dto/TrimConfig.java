package com.aivideoeraser.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrimConfig {
    private Double startTimeSeconds;
    private Double endTimeSeconds;
}
