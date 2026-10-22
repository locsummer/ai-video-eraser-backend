package com.aivideoeraser.controller;

import com.aivideoeraser.dto.ApiResponse;
import com.aivideoeraser.dto.SubscribeRequest;
import com.aivideoeraser.dto.SubscriptionPlanDto;
import com.aivideoeraser.dto.SubscriptionResponse;
import com.aivideoeraser.security.UserPrincipal;
import com.aivideoeraser.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Subscriptions & Plans", description = "Endpoints for viewing plans and managing VIP/Pro memberships")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    @Operation(summary = "Get list of available VIP and Pro membership tiers and pricing")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDto>>> getPlans() {
        List<SubscriptionPlanDto> plans = subscriptionService.getAvailablePlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @PostMapping("/subscriptions/subscribe")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Subscribe or upgrade to a Pro/VIP plan")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @Valid @RequestBody SubscribeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SubscriptionResponse response = subscriptionService.subscribe(request, principal);
        return ResponseEntity.ok(ApiResponse.success("Subscription upgraded successfully", response));
    }

    @GetMapping("/subscriptions/my-subscription")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get current user's active membership subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal UserPrincipal principal) {
        SubscriptionResponse response = subscriptionService.getMySubscription(principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
