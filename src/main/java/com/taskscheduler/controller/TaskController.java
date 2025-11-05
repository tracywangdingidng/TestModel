package com.taskscheduler.controller;

import com.taskscheduler.enums.TaskStatus;
import com.taskscheduler.model.Task;
import com.taskscheduler.model.TaskDependency;
import com.taskscheduler.service.TaskDependencyService;
import com.taskscheduler.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskDependencyService taskDependencyService;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        Task updatedTask = taskService.updateTask(task);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id, @PathVariable TaskStatus status) {
        Task task = taskService.updateTaskStatus(id, status);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable TaskStatus status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @GetMapping("/ready")
    public ResponseEntity<List<Task>> getReadyTasks() {
        List<Task> tasks = taskService.getReadyTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @GetMapping("/running")
    public ResponseEntity<List<Task>> getRunningTasks() {
        List<Task> tasks = taskService.getRunningTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @PostMapping("/{id}/dependencies")
    public ResponseEntity<TaskDependency> addDependency(@PathVariable Long id, @RequestBody TaskDependency dependency) {
        dependency.setTaskId(id);
        TaskDependency createdDependency = taskDependencyService.addDependency(dependency);
        return new ResponseEntity<>(createdDependency, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/dependencies")
    public ResponseEntity<List<TaskDependency>> getDependencies(@PathVariable Long id) {
        List<TaskDependency> dependencies = taskDependencyService.getDependenciesByTaskId(id);
        return new ResponseEntity<>(dependencies, HttpStatus.OK);
    }

    @DeleteMapping("/dependencies/{dependencyId}")
    public ResponseEntity<Void> removeDependency(@PathVariable Long dependencyId) {
        taskDependencyService.removeDependency(dependencyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
