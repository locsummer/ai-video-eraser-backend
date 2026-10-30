package com.aivideoeraser.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointDto {
    private double x;
    private double y;
    private Double timeSeconds;
    private Integer frameIndex;
}
