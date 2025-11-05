package com.taskscheduler.repository;

import com.taskscheduler.enums.TaskStatus;
import com.taskscheduler.enums.TaskType;
import com.taskscheduler.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTaskTypeAndStatus(TaskType taskType, TaskStatus status);

    List<Task> findByParentId(Long parentId);

    List<Task> findByStatusIn(List<TaskStatus> statuses);

    @Query("SELECT t FROM Task t WHERE t.status = 'WAITING' AND (t.parentId IS NULL OR EXISTS (SELECT 1 FROM Task pt WHERE pt.id = t.parentId AND pt.status = 'COMPLETED'))")
    List<Task> findReadyTasks();

    @Query("SELECT t FROM Task t WHERE t.status = 'RUNNING' ORDER BY t.priority DESC")
    List<Task> findRunningTasksByPriorityDesc();

    long countByStatus(TaskStatus status);
}