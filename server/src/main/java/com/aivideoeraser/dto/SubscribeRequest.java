package com.aivideoeraser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscribeRequest {
    @NotBlank(message = "Plan ID is required")
    private String planId; // e.g. "PRO_MONTHLY", "PRO_YEARLY", "VIP_LIFETIME"
    private String paymentToken; // e.g. Apple StoreKit receipt / Google Play billing token / Stripe token
}
