package com.taskscheduler.service;

import com.taskscheduler.enums.TaskStatus;
import com.taskscheduler.enums.TaskType;
import com.taskscheduler.model.Task;
import com.taskscheduler.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDependencyService taskDependencyService;

    @Autowired
    private TaskExecutionService taskExecutionService;

    public Task createTask(Task task) {
        log.info("Creating task: {}", task.getTaskName());
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with id: {}", savedTask.getId());
        return savedTask;
    }

    public Task updateTask(Task task) {
        log.info("Updating task: {}", task.getId());
        Optional<Task> existingTask = taskRepository.findById(task.getId());
        if (existingTask.isPresent()) {
            Task updatedTask = taskRepository.save(task);
            log.info("Task updated successfully: {}", updatedTask.getId());
            return updatedTask;
        } else {
            log.error("Task not found: {}", task.getId());
            throw new RuntimeException("Task not found: " + task.getId());
        }
    }

    public void deleteTask(Long taskId) {
        log.info("Deleting task: {}", taskId);
        taskDependencyService.deleteDependenciesByTaskId(taskId);
        taskExecutionService.deleteExecutionsByTaskId(taskId);
        taskRepository.deleteById(taskId);
        log.info("Task deleted successfully: {}", taskId);
    }

    public Task getTaskById(Long taskId) {
        log.info("Getting task by id: {}", taskId);
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
    }

    public List<Task> getAllTasks() {
        log.info("Getting all tasks");
        return taskRepository.findAll();
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        log.info("Getting tasks by status: {}", status);
        return taskRepository.findByStatusIn(Arrays.asList(status));
    }

    public List<Task> getReadyTasks() {
        log.info("Getting ready tasks");
        return taskRepository.findReadyTasks();
    }

    public List<Task> getRunningTasks() {
        log.info("Getting running tasks");
        return taskRepository.findRunningTasksByPriorityDesc();
    }

    public Task updateTaskStatus(Long taskId, TaskStatus status) {
        log.info("Updating task status: {} to {}", taskId, status);
        Task task = getTaskById(taskId);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public long getTaskCountByStatus(TaskStatus status) {
        log.info("Getting task count by status: {}", status);
        return taskRepository.countByStatus(status);
    }

    public List<Task> getTasksByType(TaskType type) {
        log.info("Getting tasks by type: {}", type);
        return taskRepository.findByTaskTypeAndStatus(type, TaskStatus.WAITING);
    }
}