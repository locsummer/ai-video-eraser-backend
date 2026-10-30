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
public class UserProfileResponse {
    private Long id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private Role role;
    private UserStatus status;
    private Integer credits;
    private SubscriptionResponse activeSubscription;
    private LocalDateTime createdAt;
}
