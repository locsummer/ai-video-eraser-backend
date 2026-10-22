package com.aivideoeraser.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCreditsDto {
    @NotNull(message = "Credits amount is required")
    @Min(value = 1, message = "Credits to add must be at least 1")
    private Integer credits;
}
