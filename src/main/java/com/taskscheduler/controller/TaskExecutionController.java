package com.taskscheduler.controller;

import com.taskscheduler.model.Task;
import com.taskscheduler.model.ExecutionNode;
import com.taskscheduler.model.TaskExecution;
import com.taskscheduler.model.TaskLog;
import com.taskscheduler.model.TaskResult;
import com.taskscheduler.service.TaskExecutionService;
import com.taskscheduler.service.TaskService;
import com.taskscheduler.service.ExecutionNodeService;
import com.taskscheduler.executor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/executions")
public class TaskExecutionController {

    @Autowired
    private TaskExecutionService taskExecutionService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ExecutionNodeService executionNodeService;

    @Autowired
    private TaskExecutor taskExecutor;

    @PostMapping("/task/{taskId}")
    public ResponseEntity<TaskExecution> startTaskExecution(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId);
        // 简单起见，这里选择第一个活跃节点执行任务
        List<ExecutionNode> activeNodes = executionNodeService.getActiveNodes();
        if (activeNodes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
        TaskExecution execution = taskExecutionService.createExecution(taskId, activeNodes.get(0).getId());
        taskExecutor.executeTask(execution.getExecutionId());
        return new ResponseEntity<>(execution, HttpStatus.CREATED);
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<TaskExecution> getExecution(@PathVariable String executionId) {
        TaskExecution execution = taskExecutionService.getExecutionByExecutionId(executionId);
        return new ResponseEntity<>(execution, HttpStatus.OK);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskExecution>> getExecutionsByTask(@PathVariable Long taskId) {
        List<TaskExecution> executions = taskExecutionService.getExecutionsByTaskId(taskId);
        return new ResponseEntity<>(executions, HttpStatus.OK);
    }

    @GetMapping("/{executionId}/logs")
    public ResponseEntity<List<TaskLog>> getExecutionLogs(@PathVariable String executionId) {
        List<TaskLog> logs = taskExecutionService.getExecutionLogs(executionId);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @GetMapping("/{executionId}/result")
    public ResponseEntity<TaskResult> getExecutionResult(@PathVariable String executionId) {
        TaskResult result = taskExecutionService.getExecutionResult(executionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/{executionId}/cancel")
    public ResponseEntity<Void> cancelExecution(@PathVariable String executionId) {
        taskExecutor.cancelTask(executionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{executionId}/pause")
    public ResponseEntity<Void> pauseExecution(@PathVariable String executionId) {
        taskExecutor.pauseTask(executionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{executionId}/resume")
    public ResponseEntity<Void> resumeExecution(@PathVariable String executionId) {
        taskExecutor.resumeTask(executionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/running")
    public ResponseEntity<List<TaskExecution>> getRunningExecutions() {
        List<TaskExecution> executions = taskExecutionService.getRunningExecutions();
        return new ResponseEntity<>(executions, HttpStatus.OK);
    }
}