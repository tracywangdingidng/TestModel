package com.taskscheduler.repository;

import com.taskscheduler.model.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {
    List<TaskLog> findByExecutionId(String executionId);
    List<TaskLog> findByExecutionIdOrderByLogTimeDesc(String executionId);
}