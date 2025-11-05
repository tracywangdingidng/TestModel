package com.taskscheduler.repository;

import com.taskscheduler.model.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    List<TaskDependency> findByTaskId(Long taskId);
    List<TaskDependency> findByDependencyTaskId(Long dependencyTaskId);
    void deleteByTaskId(Long taskId);
}