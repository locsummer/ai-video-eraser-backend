package com.aivideoeraser.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    private Long id;
    private String taskId;
    private Long userId;
    private Integer rating;
    private String comment;
    private String category;
    private LocalDateTime createdAt;
}
