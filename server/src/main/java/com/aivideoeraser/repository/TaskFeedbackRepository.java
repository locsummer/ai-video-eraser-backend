package com.aivideoeraser.repository;

import com.aivideoeraser.entity.EraseTask;
import com.aivideoeraser.entity.TaskFeedback;
import com.aivideoeraser.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskFeedbackRepository extends JpaRepository<TaskFeedback, Long> {
    Optional<TaskFeedback> findByTask(EraseTask task);
    Page<TaskFeedback> findByUser(User user, Pageable pageable);
}
