package com.aivideoeraser.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaskData {
    // Custom rectangle / ellipse mask tracking
    private RectDto boundingBox;
    private List<RectDto> frameBoxes;

    // AI Object tracking click / seed points (e.g. SAM / Tracking points)
    private List<PointDto> trackingPoints;

    // Freehand brush stroke points
    private List<PointDto> brushStroke;

    // Mask shape type (RECTANGLE, ELLIPSE, BRUSH, SAM_POINTS)
    private String shapeType;

    // Brush stroke width if freehand brush is used
    private Double brushRadius;
}
