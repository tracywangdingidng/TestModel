package com.taskscheduler.repository;

import com.taskscheduler.model.TaskResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskResultRepository extends JpaRepository<TaskResult, Long> {
    Optional<TaskResult> findByExecutionId(String executionId);
    List<TaskResult> findByTaskId(Long taskId);
    long countByResultStatus(TaskResult.ResultStatus resultStatus);
}