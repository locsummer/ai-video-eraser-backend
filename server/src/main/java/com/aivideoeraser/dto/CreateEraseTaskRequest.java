package com.aivideoeraser.dto;

import com.aivideoeraser.entity.EraseMode;
import com.aivideoeraser.entity.ModelEngine;
import com.aivideoeraser.entity.QualityMode;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEraseTaskRequest {

    @NotNull(message = "Video ID is required")
    private Long videoId;

    @NotNull(message = "Erase mode is required")
    private EraseMode eraseMode;

    @Builder.Default
    private ModelEngine modelEngine = ModelEngine.PRO_PAINTER;

    @Builder.Default
    private QualityMode qualityMode = QualityMode.STANDARD;

    @Builder.Default
    private Boolean autoTracking = true;

    private TrimConfig trimConfig;

    @NotNull(message = "Mask data is required")
    private MaskData maskData;
}
