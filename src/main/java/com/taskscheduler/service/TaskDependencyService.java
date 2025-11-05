package com.taskscheduler.service;

import com.taskscheduler.model.TaskDependency;
import com.taskscheduler.repository.TaskDependencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class TaskDependencyService {

    @Autowired
    private TaskDependencyRepository taskDependencyRepository;

    public TaskDependency addDependency(TaskDependency dependency) {
        log.info("Adding dependency: Task {} -> Dependency {}", 
                dependency.getTaskId(), dependency.getDependencyTaskId());
        return taskDependencyRepository.save(dependency);
    }

    public void removeDependency(Long dependencyId) {
        log.info("Removing dependency: {}", dependencyId);
        taskDependencyRepository.deleteById(dependencyId);
    }

    public void deleteDependenciesByTaskId(Long taskId) {
        log.info("Deleting all dependencies for task: {}", taskId);
        taskDependencyRepository.deleteByTaskId(taskId);
    }

    public List<TaskDependency> getDependenciesByTaskId(Long taskId) {
        log.info("Getting dependencies for task: {}", taskId);
        return taskDependencyRepository.findByTaskId(taskId);
    }

    public List<TaskDependency> getDependentsByTaskId(Long taskId) {
        log.info("Getting dependents for task: {}", taskId);
        return taskDependencyRepository.findByDependencyTaskId(taskId);
    }
}