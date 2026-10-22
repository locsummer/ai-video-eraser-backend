package com.aivideoeraser.repository;

import com.aivideoeraser.entity.Role;
import com.aivideoeraser.entity.User;
import com.aivideoeraser.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    Page<User> findByRole(Role role, Pageable pageable);
    long countByStatus(UserStatus status);
}
