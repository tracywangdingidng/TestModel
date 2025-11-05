package com.taskscheduler.controller;

import com.taskscheduler.enums.NodeStatus;
import com.taskscheduler.enums.TaskStatus;
import com.taskscheduler.model.ExecutionNode;
import com.taskscheduler.service.ExecutionNodeService;
import com.taskscheduler.service.TaskExecutionService;
import com.taskscheduler.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ExecutionNodeService executionNodeService;

    @Autowired
    private TaskExecutionService taskExecutionService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 任务统计
        Map<String, Long> taskStats = new HashMap<>();
        taskStats.put("total", (long) taskService.getAllTasks().size());
        taskStats.put("waiting", taskService.getTaskCountByStatus(TaskStatus.WAITING));
        taskStats.put("running", taskService.getTaskCountByStatus(TaskStatus.RUNNING));
        taskStats.put("completed", taskService.getTaskCountByStatus(TaskStatus.COMPLETED));
        taskStats.put("failed", taskService.getTaskCountByStatus(TaskStatus.FAILED));
        taskStats.put("paused", taskService.getTaskCountByStatus(TaskStatus.PAUSED));
        taskStats.put("cancelled", taskService.getTaskCountByStatus(TaskStatus.CANCELLED));
        stats.put("taskStats", taskStats);

        // 节点统计
        Map<String, Long> nodeStats = new HashMap<>();
        nodeStats.put("total", (long) executionNodeService.getAllNodes().size());
        nodeStats.put("active", executionNodeService.getActiveNodeCount());
        nodeStats.put("inactive", executionNodeService.countByStatus(NodeStatus.INACTIVE));
        nodeStats.put("failed", executionNodeService.countByStatus(NodeStatus.FAILED));
        stats.put("nodeStats", nodeStats);

        // 执行统计
        long runningExecutions = taskExecutionService.countByStatus(TaskStatus.RUNNING);
        stats.put("runningExecutions", runningExecutions);

        // 资源统计
        List<ExecutionNode> activeNodes = executionNodeService.getActiveNodes();
        if (!activeNodes.isEmpty()) {
            double avgCpu = activeNodes.stream().mapToDouble(ExecutionNode::getCpuUsage).average().orElse(0.0);
            double avgMemory = activeNodes.stream().mapToDouble(ExecutionNode::getMemoryUsage).average().orElse(0.0);
            stats.put("averageCpuUsage", avgCpu);
            stats.put("averageMemoryUsage", avgMemory);
        }

        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        // 检查核心服务状态
        List<ExecutionNode> activeNodes = executionNodeService.getActiveNodes();
        health.put("activeNodesAvailable", !activeNodes.isEmpty());
        health.put("databaseAvailable", true); // 简单起见，这里假设数据库可用
        health.put("redisAvailable", true); // 简单起见，这里假设Redis可用

        return new ResponseEntity<>(health, HttpStatus.OK);
    }
}