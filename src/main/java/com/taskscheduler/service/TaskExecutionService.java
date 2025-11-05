package com.taskscheduler.service;

import com.taskscheduler.enums.TaskStatus;
import com.taskscheduler.model.TaskExecution;
import com.taskscheduler.model.TaskLog;
import com.taskscheduler.model.TaskResult;
import com.taskscheduler.repository.TaskExecutionRepository;
import com.taskscheduler.repository.TaskLogRepository;
import com.taskscheduler.repository.TaskResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class TaskExecutionService {

    @Autowired
    private TaskExecutionRepository taskExecutionRepository;

    @Autowired
    private TaskResultRepository taskResultRepository;

    @Autowired
    private TaskLogRepository taskLogRepository;

    public TaskExecution createExecution(Long taskId, Long nodeId) {
        log.info("Creating execution for task: {} on node: {}", taskId, nodeId);
        TaskExecution execution = new TaskExecution();
        execution.setTaskId(taskId);
        execution.setExecutionNodeId(nodeId);
        execution.setExecutionId(generateExecutionId());
        execution.setStatus(TaskStatus.RUNNING);
        execution.setStartTime(new Date());
        return taskExecutionRepository.save(execution);
    }

    public TaskExecution updateExecutionStatus(String executionId, TaskStatus status) {
        log.info("Updating execution status: {} to {}", executionId, status);
        TaskExecution execution = getExecutionByExecutionId(executionId);
        execution.setStatus(status);
        if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED || status == TaskStatus.CANCELLED) {
            execution.setEndTime(new Date());
            if (execution.getStartTime() != null) {
                execution.setDuration(execution.getEndTime().getTime() - execution.getStartTime().getTime());
            }
        }
        return taskExecutionRepository.save(execution);
    }

    public TaskResult saveExecutionResult(String executionId, String resultData, TaskResult.ResultStatus status) {
        log.info("Saving execution result: {} status: {}", executionId, status);
        TaskExecution execution = getExecutionByExecutionId(executionId);
        TaskResult result = new TaskResult();
        result.setExecutionId(executionId);
        result.setTaskId(execution.getTaskId());
        result.setResultData(resultData);
        result.setResultStatus(status);
        return taskResultRepository.save(result);
    }

    public void saveTaskLog(String executionId, TaskLog.LogLevel level, String message) {
        log.info("Saving task log: {} - {}: {}", executionId, level, message);
        TaskLog logEntry = new TaskLog();
        logEntry.setExecutionId(executionId);
        logEntry.setLogLevel(level);
        logEntry.setLogMessage(message);
        taskLogRepository.save(logEntry);
    }

    public TaskExecution getExecutionByExecutionId(String executionId) {
        return taskExecutionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));
    }

    public List<TaskExecution> getExecutionsByTaskId(Long taskId) {
        return taskExecutionRepository.findByTaskId(taskId);
    }

    public List<TaskExecution> getRunningExecutions() {
        return taskExecutionRepository.findByStatus(TaskStatus.RUNNING);
    }

    public long countByStatus(TaskStatus status) {
        log.info("Counting executions by status: {}", status);
        return taskExecutionRepository.countByStatus(status);
    }

    public void deleteExecutionsByTaskId(Long taskId) {
        log.info("Deleting executions for task: {}", taskId);
        List<TaskExecution> executions = taskExecutionRepository.findByTaskId(taskId);
        executions.forEach(execution -> {
            taskLogRepository.deleteAll(taskLogRepository.findByExecutionId(execution.getExecutionId()));
            taskResultRepository.deleteById(execution.getId());
        });
        taskExecutionRepository.deleteAll(executions);
    }

    public TaskResult getExecutionResult(String executionId) {
        return taskResultRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new RuntimeException("Result not found: " + executionId));
    }

    public List<TaskLog> getExecutionLogs(String executionId) {
        return taskLogRepository.findByExecutionIdOrderByLogTimeDesc(executionId);
    }

    private String generateExecutionId() {
        return "EXEC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}