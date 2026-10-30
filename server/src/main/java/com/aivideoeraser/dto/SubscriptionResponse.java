package com.aivideoeraser.dto;

import com.aivideoeraser.entity.SubscriptionStatus;
import com.aivideoeraser.entity.SubscriptionTier;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {
    private Long id;
    private String planName;
    private SubscriptionTier tier;
    private Integer maxDurationSeconds;
    private String maxResolution;
    private Boolean hasWatermark;
    private Boolean priorityQueue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SubscriptionStatus status;
    private BigDecimal amountPaid;
    private LocalDateTime createdAt;
}
