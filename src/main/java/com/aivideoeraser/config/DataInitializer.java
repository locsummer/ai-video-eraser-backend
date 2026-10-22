package com.aivideoeraser.config;

import com.aivideoeraser.entity.*;
import com.aivideoeraser.repository.SubscriptionRepository;
import com.aivideoeraser.repository.SystemSettingRepository;
import com.aivideoeraser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdminUser();
        initDemoUser();
        initSystemSettings();
    }

    private void initAdminUser() {
        if (!userRepository.existsByEmail("admin@aivideoeraser.app")) {
            User admin = User.builder()
                    .email("admin@aivideoeraser.app")
                    .password(passwordEncoder.encode("Admin@123456"))
                    .fullName("System Administrator")
                    .role(Role.ROLE_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .credits(99999)
                    .build();
            userRepository.save(admin);
            log.info("Initialized default admin user: admin@aivideoeraser.app");
        }
    }

    private void initDemoUser() {
        if (!userRepository.existsByEmail("demo@aivideoeraser.app")) {
            User demo = User.builder()
                    .email("demo@aivideoeraser.app")
                    .password(passwordEncoder.encode("Demo@123456"))
                    .fullName("VIP Demo User")
                    .role(Role.ROLE_PREMIUM)
                    .status(UserStatus.ACTIVE)
                    .credits(500)
                    .build();
            User savedDemo = userRepository.save(demo);

            Subscription vipSub = Subscription.builder()
                    .user(savedDemo)
                    .planName("VIP Lifetime")
                    .tier(SubscriptionTier.VIP)
                    .maxDurationSeconds(300)
                    .maxResolution("4K")
                    .hasWatermark(false)
                    .priorityQueue(true)
                    .startDate(LocalDateTime.now())
                    .status(SubscriptionStatus.ACTIVE)
                    .amountPaid(new BigDecimal("99.99"))
                    .build();
            subscriptionRepository.save(vipSub);

            log.info("Initialized VIP demo user: demo@aivideoeraser.app");
        }
    }

    private void initSystemSettings() {
        createSettingIfAbsent("max_upload_size_mb", "500", "Maximum video upload size in Megabytes");
        createSettingIfAbsent("default_model_engine", "PRO_PAINTER", "Default AI inpainting engine");
        createSettingIfAbsent("free_tier_max_duration", "15", "Maximum video processing duration for free tier in seconds");
        createSettingIfAbsent("worker_heartbeat_timeout_seconds", "60", "Seconds before GPU worker job times out");
    }

    private void createSettingIfAbsent(String key, String value, String description) {
        if (systemSettingRepository.findBySettingKey(key).isEmpty()) {
            systemSettingRepository.save(SystemSetting.builder()
                    .settingKey(key)
                    .settingValue(value)
                    .description(description)
                    .build());
        }
    }
}
