package com.taskscheduler.repository;

import com.taskscheduler.enums.TaskStatus;
import com.taskscheduler.model.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {
    Optional<TaskExecution> findByExecutionId(String executionId);
    List<TaskExecution> findByTaskId(Long taskId);
    List<TaskExecution> findByStatus(TaskStatus status);
    List<TaskExecution> findByExecutionNodeId(Long executionNodeId);
    long countByStatus(TaskStatus status);
}