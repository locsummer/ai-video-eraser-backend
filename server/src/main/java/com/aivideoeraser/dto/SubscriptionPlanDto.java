package com.aivideoeraser.dto;

import com.aivideoeraser.entity.SubscriptionTier;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanDto {
    private String id;
    private String name;
    private SubscriptionTier tier;
    private BigDecimal price;
    private String currency;
    private String period; // e.g. "month", "year", "one-time"
    private int maxDurationSeconds;
    private String maxResolution;
    private boolean watermarkFree;
    private boolean priorityQueue;
    private int monthlyCredits;
    private List<String> features;
}
