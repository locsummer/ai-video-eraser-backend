package com.aivideoeraser.service;

import com.aivideoeraser.dto.SubscribeRequest;
import com.aivideoeraser.dto.SubscriptionPlanDto;
import com.aivideoeraser.dto.SubscriptionResponse;
import com.aivideoeraser.entity.*;
import com.aivideoeraser.exception.BadRequestException;
import com.aivideoeraser.exception.ResourceNotFoundException;
import com.aivideoeraser.repository.SubscriptionRepository;
import com.aivideoeraser.repository.UserRepository;
import com.aivideoeraser.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public List<SubscriptionPlanDto> getAvailablePlans() {
        return Arrays.asList(
                SubscriptionPlanDto.builder()
                        .id("FREE")
                        .name("Free Starter")
                        .tier(SubscriptionTier.FREE)
                        .price(BigDecimal.ZERO)
                        .currency("USD")
                        .period("lifetime")
                        .maxDurationSeconds(15)
                        .maxResolution("720p")
                        .watermarkFree(false)
                        .priorityQueue(false)
                        .monthlyCredits(50)
                        .features(Arrays.asList("Up to 15s video length", "720p resolution export", "Standard queue", "Standard AI Eraser"))
                        .build(),
                SubscriptionPlanDto.builder()
                        .id("PRO_MONTHLY")
                        .name("Pro Monthly")
                        .tier(SubscriptionTier.PRO)
                        .price(new BigDecimal("9.99"))
                        .currency("USD")
                        .period("month")
                        .maxDurationSeconds(60)
                        .maxResolution("1080p")
                        .watermarkFree(true)
                        .priorityQueue(true)
                        .monthlyCredits(300)
                        .features(Arrays.asList("Up to 60s video length", "1080p Full HD export", "No watermark", "Fast GPU Priority Queue", "ProPainter & E2FGVI engines"))
                        .build(),
                SubscriptionPlanDto.builder()
                        .id("PRO_YEARLY")
                        .name("Pro Yearly")
                        .tier(SubscriptionTier.PRO)
                        .price(new BigDecimal("59.99"))
                        .currency("USD")
                        .period("year")
                        .maxDurationSeconds(120)
                        .maxResolution("1080p")
                        .watermarkFree(true)
                        .priorityQueue(true)
                        .monthlyCredits(500)
                        .features(Arrays.asList("Up to 120s video length", "1080p Full HD export", "No watermark", "Priority GPU Queue", "Save 50% vs Monthly"))
                        .build(),
                SubscriptionPlanDto.builder()
                        .id("VIP_LIFETIME")
                        .name("VIP Lifetime")
                        .tier(SubscriptionTier.VIP)
                        .price(new BigDecimal("99.99"))
                        .currency("USD")
                        .period("lifetime")
                        .maxDurationSeconds(300)
                        .maxResolution("4K")
                        .watermarkFree(true)
                        .priorityQueue(true)
                        .monthlyCredits(9999)
                        .features(Arrays.asList("Unlimited duration up to 300s", "4K Ultra HD Export", "No watermark", "Ultra Priority Dedicated GPU Queue", "All AI Models Included"))
                        .build()
        );
    }

    @Transactional
    public SubscriptionResponse subscribe(SubscribeRequest request, UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        List<SubscriptionPlanDto> plans = getAvailablePlans();
        SubscriptionPlanDto selectedPlan = plans.stream()
                .filter(p -> p.getId().equalsIgnoreCase(request.getPlanId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid subscription plan ID: " + request.getPlanId()));

        // Mark previous active subscriptions as EXPIRED
        List<Subscription> existingSubs = subscriptionRepository.findByUserOrderByCreatedAtDesc(user);
        for (Subscription sub : existingSubs) {
            if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = null;
        if ("month".equalsIgnoreCase(selectedPlan.getPeriod())) {
            endDate = now.plusMonths(1);
        } else if ("year".equalsIgnoreCase(selectedPlan.getPeriod())) {
            endDate = now.plusYears(1);
        } // "lifetime" has null endDate

        Subscription newSubscription = Subscription.builder()
                .user(user)
                .planName(selectedPlan.getName())
                .tier(selectedPlan.getTier())
                .maxDurationSeconds(selectedPlan.getMaxDurationSeconds())
                .maxResolution(selectedPlan.getMaxResolution())
                .hasWatermark(!selectedPlan.isWatermarkFree())
                .priorityQueue(selectedPlan.isPriorityQueue())
                .startDate(now)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .amountPaid(selectedPlan.getPrice())
                .build();

        Subscription savedSub = subscriptionRepository.save(newSubscription);

        // Grant bonus plan credits and upgrade user role
        user.setCredits(user.getCredits() + selectedPlan.getMonthlyCredits());
        if (selectedPlan.getTier() != SubscriptionTier.FREE) {
            user.setRole(Role.ROLE_PREMIUM);
        }
        userRepository.save(user);

        log.info("User {} successfully subscribed to {}", user.getEmail(), selectedPlan.getName());

        return mapToSubscriptionResponse(savedSub);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getMySubscription(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Subscription activeSub = subscriptionRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found for user"));

        return mapToSubscriptionResponse(activeSub);
    }

    private SubscriptionResponse mapToSubscriptionResponse(Subscription sub) {
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .planName(sub.getPlanName())
                .tier(sub.getTier())
                .maxDurationSeconds(sub.getMaxDurationSeconds())
                .maxResolution(sub.getMaxResolution())
                .hasWatermark(sub.getHasWatermark())
                .priorityQueue(sub.getPriorityQueue())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .status(sub.getStatus())
                .amountPaid(sub.getAmountPaid())
                .createdAt(sub.getCreatedAt())
                .build();
    }
}
