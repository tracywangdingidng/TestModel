package com.taskscheduler.executor;

import com.taskscheduler.enums.TaskStatus;
import com.taskscheduler.model.Task;
import com.taskscheduler.model.TaskExecution;
import com.taskscheduler.model.TaskLog;
import com.taskscheduler.model.TaskResult;
import com.taskscheduler.service.TaskExecutionService;
import com.taskscheduler.service.TaskService;
import com.taskscheduler.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class TaskExecutor {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskExecutionService taskExecutionService;

    @Autowired
    private WebSocketService webSocketService;

    private ExecutorService executorService = Executors.newFixedThreadPool(20);
    private Map<String, Future<?>> executionFutures = new ConcurrentHashMap<>();
    private Map<String, AtomicBoolean> cancellationFlags = new ConcurrentHashMap<>();
    private Map<String, AtomicBoolean> pauseFlags = new ConcurrentHashMap<>();

    public void executeTask(String executionId) {
        log.info("Executing task with executionId: {}", executionId);
        TaskExecution execution = taskExecutionService.getExecutionByExecutionId(executionId);
        Task task = taskService.getTaskById(execution.getTaskId());

        AtomicBoolean cancelFlag = new AtomicBoolean(false);
        AtomicBoolean pauseFlag = new AtomicBoolean(false);
        cancellationFlags.put(executionId, cancelFlag);
        pauseFlags.put(executionId, pauseFlag);

        Future<?> future = executorService.submit(() -> {
            try {
                taskExecutionService.saveTaskLog(executionId, TaskLog.LogLevel.INFO, "Task execution started");
                webSocketService.sendTaskExecutionUpdate(execution);

                // 模拟任务执行
                int progress = 0;
                while (progress < 100 && !cancelFlag.get()) {
                    if (pauseFlag.get()) {
                        taskExecutionService.saveTaskLog(executionId, TaskLog.LogLevel.INFO, "Task paused");
                        execution.setStatus(TaskStatus.PAUSED);
                        taskService.updateTaskStatus(task.getId(), TaskStatus.PAUSED);
                        webSocketService.sendTaskExecutionUpdate(execution);
                        webSocketService.sendTaskStatusUpdate(task);

                        // 等待恢复信号
                        while (pauseFlag.get() && !cancelFlag.get()) {
                            Thread.sleep(1000);
                        }

                        if (cancelFlag.get()) {
                            break;
                        }

                        taskExecutionService.saveTaskLog(executionId, TaskLog.LogLevel.INFO, "Task resumed");
                        execution.setStatus(TaskStatus.RUNNING);
                        taskService.updateTaskStatus(task.getId(), TaskStatus.RUNNING);
                        webSocketService.sendTaskExecutionUpdate(execution);
                        webSocketService.sendTaskStatusUpdate(task);
                    }

                    // 执行任务逻辑（模拟）
                    Thread.sleep(1000);
                    progress += 10;

                    // 发送进度更新
                    Map<String, Object> progressUpdate = new HashMap<>();
                    progressUpdate.put("executionId", executionId);
                    progressUpdate.put("taskId", task.getId());
                    progressUpdate.put("progress", progress);
                    progressUpdate.put("status", "RUNNING");
                    webSocketService.sendTaskResultUpdate(executionId, progressUpdate);
                }

                if (cancelFlag.get()) {
                    log.info("Task execution cancelled: {}", executionId);
                    taskExecutionService.saveTaskLog(executionId, TaskLog.LogLevel.WARN, "Task execution cancelled");
                    taskExecutionService.updateExecutionStatus(executionId, TaskStatus.CANCELLED);
                    taskService.updateTaskStatus(task.getId(), TaskStatus.CANCELLED);
                    taskExecutionService.saveExecutionResult(executionId, "Task cancelled", TaskResult.ResultStatus.FAILURE);
                } else {
                    log.info("Task execution completed successfully: {}", executionId);
                    taskExecutionService.saveTaskLog(executionId, TaskLog.LogLevel.INFO, "Task execution completed");
                    taskExecutionService.updateExecutionStatus(executionId, TaskStatus.COMPLETED);
                    taskService.updateTaskStatus(task.getId(), TaskStatus.COMPLETED);

                    // 模拟任务结果
                    String resultData = String.format("Task executed successfully. Result: %s", 
                            "Sample result data for task " + task.getId());
                    taskExecutionService.saveExecutionResult(executionId, resultData, TaskResult.ResultStatus.SUCCESS);
                }

            } catch (InterruptedException e) {
                log.error("Task execution interrupted: {}", executionId, e);
                taskExecutionService.saveTaskLog(executionId, TaskLog.LogLevel.ERROR, "Task execution interrupted: " + e.getMessage());
                taskExecutionService.updateExecutionStatus(executionId, TaskStatus.FAILED);
                taskService.updateTaskStatus(task.getId(), TaskStatus.FAILED);
                taskExecutionService.saveExecutionResult(executionId, "Execution interrupted", TaskResult.ResultStatus.FAILURE);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error executing task: {}", executionId, e);
                taskExecutionService.saveTaskLog(executionId, TaskLog.LogLevel.ERROR, "Task execution failed: " + e.getMessage());
                taskExecutionService.updateExecutionStatus(executionId, TaskStatus.FAILED);
                taskService.updateTaskStatus(task.getId(), TaskStatus.FAILED);
                taskExecutionService.saveExecutionResult(executionId, "Execution failed: " + e.getMessage(), TaskResult.ResultStatus.FAILURE);
            } finally {
                executionFutures.remove(executionId);
                cancellationFlags.remove(executionId);
                pauseFlags.remove(executionId);
                webSocketService.sendTaskExecutionUpdate(execution);
                webSocketService.sendTaskStatusUpdate(task);
            }
        });

        executionFutures.put(executionId, future);
    }

    public void cancelTask(String executionId) {
        log.info("Cancelling task execution: {}", executionId);
        AtomicBoolean cancelFlag = cancellationFlags.get(executionId);
        if (cancelFlag != null) {
            cancelFlag.set(true);
        }

        Future<?> future = executionFutures.get(executionId);
        if (future != null) {
            future.cancel(true);
        }
    }

    public void pauseTask(String executionId) {
        log.info("Pausing task execution: {}", executionId);
        AtomicBoolean pauseFlag = pauseFlags.get(executionId);
        if (pauseFlag != null) {
            pauseFlag.set(true);
        }
    }

    public void resumeTask(String executionId) {
        log.info("Resuming task execution: {}", executionId);
        AtomicBoolean pauseFlag = pauseFlags.get(executionId);
        if (pauseFlag != null) {
            pauseFlag.set(false);
        }
    }

    public boolean isTaskRunning(String executionId) {
        Future<?> future = executionFutures.get(executionId);
        return future != null && !future.isDone() && !future.isCancelled();
    }

    public boolean isTaskPaused(String executionId) {
        AtomicBoolean pauseFlag = pauseFlags.get(executionId);
        return pauseFlag != null && pauseFlag.get();
    }
}