package com.aivideoeraser.repository;

import com.aivideoeraser.entity.EraseMode;
import com.aivideoeraser.entity.EraseTask;
import com.aivideoeraser.entity.TaskStatus;
import com.aivideoeraser.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EraseTaskRepository extends JpaRepository<EraseTask, String> {
    Optional<EraseTask> findByIdAndUser(String id, User user);
    Page<EraseTask> findByUser(User user, Pageable pageable);
    Page<EraseTask> findByUserAndStatus(User user, TaskStatus status, Pageable pageable);
    Page<EraseTask> findByUserAndEraseMode(User user, EraseMode eraseMode, Pageable pageable);
    Page<EraseTask> findByStatus(TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM EraseTask t WHERE t.status = :status ORDER BY t.createdAt ASC")
    List<EraseTask> findNextQueuedTasks(@Param("status") TaskStatus status, Pageable pageable);

    long countByUser(User user);
    long countByStatus(TaskStatus status);
}
