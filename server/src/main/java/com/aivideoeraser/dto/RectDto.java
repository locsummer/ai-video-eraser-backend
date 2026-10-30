package com.aivideoeraser.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RectDto {
    private double x;
    private double y;
    private double width;
    private double height;
    private Integer startFrame;
    private Integer endFrame;
}
