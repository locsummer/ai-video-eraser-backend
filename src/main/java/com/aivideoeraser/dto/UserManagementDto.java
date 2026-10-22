package com.aivideoeraser.dto;

import com.aivideoeraser.entity.Role;
import com.aivideoeraser.entity.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementDto {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private UserStatus status;
    private Integer credits;
    private long totalVideosUploaded;
    private long totalTasksSubmitted;
    private LocalDateTime createdAt;
}
