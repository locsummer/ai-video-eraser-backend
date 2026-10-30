package com.aivideoeraser.dto;

import com.aivideoeraser.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserStatusDto {
    @NotNull(message = "Status is required")
    private UserStatus status;
}
