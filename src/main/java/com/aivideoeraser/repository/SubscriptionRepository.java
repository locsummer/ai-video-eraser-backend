package com.aivideoeraser.repository;

import com.aivideoeraser.entity.Subscription;
import com.aivideoeraser.entity.SubscriptionStatus;
import com.aivideoeraser.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByUserAndStatusOrderByCreatedAtDesc(User user, SubscriptionStatus status);
    List<Subscription> findByUserOrderByCreatedAtDesc(User user);
    long countByStatus(SubscriptionStatus status);
}
