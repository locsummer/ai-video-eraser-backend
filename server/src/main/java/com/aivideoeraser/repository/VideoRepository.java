package com.aivideoeraser.repository;

import com.aivideoeraser.entity.User;
import com.aivideoeraser.entity.Video;
import com.aivideoeraser.entity.VideoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Page<Video> findByUserAndStatus(User user, VideoStatus status, Pageable pageable);
    Optional<Video> findByIdAndUser(Long id, User user);
    long countByUser(User user);
    long countByStatus(VideoStatus status);
}
