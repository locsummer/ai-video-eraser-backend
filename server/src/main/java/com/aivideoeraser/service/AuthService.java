package com.aivideoeraser.service;

import com.aivideoeraser.dto.*;
import com.aivideoeraser.entity.*;
import com.aivideoeraser.exception.BadRequestException;
import com.aivideoeraser.exception.ResourceNotFoundException;
import com.aivideoeraser.exception.UnauthorizedException;
import com.aivideoeraser.repository.SubscriptionRepository;
import com.aivideoeraser.repository.UserRepository;
import com.aivideoeraser.security.JwtTokenProvider;
import com.aivideoeraser.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.credits.default-free-credits:50}")
    private int defaultFreeCredits;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .credits(defaultFreeCredits)
                .build();

        User savedUser = userRepository.save(user);

        // Assign initial FREE subscription
        Subscription freeSub = Subscription.builder()
                .user(savedUser)
                .planName("FREE")
                .tier(SubscriptionTier.FREE)
                .maxDurationSeconds(15)
                .maxResolution("720p")
                .hasWatermark(true)
                .priorityQueue(false)
                .startDate(LocalDateTime.now())
                .status(SubscriptionStatus.ACTIVE)
                .amountPaid(BigDecimal.ZERO)
                .build();
        subscriptionRepository.save(freeSub);

        String token = tokenProvider.generateTokenFromUser(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId());

        log.info("New user registered successfully: {}", savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .credits(savedUser.getCredits())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new UnauthorizedException("Your account has been locked. Please contact support.");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Your account is currently suspended.");
        }

        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .credits(user.getCredits())
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        String newToken = tokenProvider.generateTokenFromUser(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .credits(user.getCredits())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Subscription activeSub = subscriptionRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, SubscriptionStatus.ACTIVE)
                .orElse(null);

        SubscriptionResponse subResponse = null;
        if (activeSub != null) {
            subResponse = SubscriptionResponse.builder()
                    .id(activeSub.getId())
                    .planName(activeSub.getPlanName())
                    .tier(activeSub.getTier())
                    .maxDurationSeconds(activeSub.getMaxDurationSeconds())
                    .maxResolution(activeSub.getMaxResolution())
                    .hasWatermark(activeSub.getHasWatermark())
                    .priorityQueue(activeSub.getPriorityQueue())
                    .startDate(activeSub.getStartDate())
                    .endDate(activeSub.getEndDate())
                    .status(activeSub.getStatus())
                    .amountPaid(activeSub.getAmountPaid())
                    .createdAt(activeSub.getCreatedAt())
                    .build();
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .credits(user.getCredits())
                .activeSubscription(subResponse)
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }

        userRepository.save(user);
        return getProfile(principal);
    }

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getEmail());
    }
}
