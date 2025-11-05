package com.taskscheduler.service;

import com.taskscheduler.enums.NodeStatus;
import com.taskscheduler.enums.TaskPriority;
import com.taskscheduler.enums.TaskStatus;
import com.taskscheduler.enums.TaskType;
import com.taskscheduler.model.ExecutionNode;
import com.taskscheduler.model.Task;
import com.taskscheduler.model.TaskExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DistributedScheduler {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ExecutionNodeService executionNodeService;

    @Autowired
    private TaskExecutionService taskExecutionService;

    @Autowired
    private RedisService redisService;

    private ConsistentHashing consistentHashing = new ConsistentHashing(100);

    private Map<String, ExecutionNode> nodeMap = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 5000) // 每5秒执行一次
    public void scheduleTasks() {
        try {
            updateNodeList();
            if (nodeMap.isEmpty()) {
                log.warn("No active nodes available for scheduling");
                return;
            }

            // 检查故障节点并重新分配任务
            checkFailedNodes();

            // 调度等待中的任务
            scheduleWaitingTasks();

            // 检查任务抢占
            checkTaskPreemption();

        } catch (Exception e) {
            log.error("Error in task scheduling:", e);
        }
    }

    private void updateNodeList() {
        List<ExecutionNode> activeNodes = executionNodeService.getActiveNodes();
        Set<String> currentNodeIds = new HashSet<>();

        for (ExecutionNode node : activeNodes) {
            currentNodeIds.add(node.getNodeId());
            if (!nodeMap.containsKey(node.getNodeId())) {
                nodeMap.put(node.getNodeId(), node);
                consistentHashing.addNode(node.getNodeId());
                log.info("Added node to scheduler: {}", node.getNodeId());
            }
        }

        // 移除失效节点
        Iterator<Map.Entry<String, ExecutionNode>> iterator = nodeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ExecutionNode> entry = iterator.next();
            if (!currentNodeIds.contains(entry.getKey())) {
                iterator.remove();
                consistentHashing.removeNode(entry.getKey());
                log.info("Removed node from scheduler: {}", entry.getKey());
            }
        }
    }

    private void checkFailedNodes() {
        List<TaskExecution> runningExecutions = taskExecutionService.getRunningExecutions();
        for (TaskExecution execution : runningExecutions) {
            ExecutionNode node = nodeMap.get(execution.getExecutionNodeId().toString());
            if (node == null || node.getStatus() != NodeStatus.ACTIVE) {
                log.warn("Node {} failed, rescheduling task {}", execution.getExecutionNodeId(), execution.getTaskId());
                taskExecutionService.updateExecutionStatus(execution.getExecutionId(), TaskStatus.FAILED);
                Task task = taskService.getTaskById(execution.getTaskId());
                if (task.getRetryCount() < task.getMaxRetries()) {
                    task.setRetryCount(task.getRetryCount() + 1);
                    task.setStatus(TaskStatus.WAITING);
                    taskService.updateTask(task);
                    log.info("Task {} will be retried (attempt {}/{})\n", task.getId(), task.getRetryCount(), task.getMaxRetries());
                } else {
                    task.setStatus(TaskStatus.FAILED);
                    taskService.updateTask(task);
                    log.error("Task {} failed permanently after {} attempts", task.getId(), task.getMaxRetries());
                }
            }
        }
    }

    private void scheduleWaitingTasks() {
        List<Task> readyTasks = taskService.getReadyTasks();
        for (Task task : readyTasks) {
            if (task.getStatus() != TaskStatus.WAITING) {
                continue;
            }

            // 使用一致性哈希选择节点
            String nodeId = consistentHashing.getNode(task.getId().toString());
            ExecutionNode node = nodeMap.get(nodeId);

            if (node != null) {
                try {
                    String lockKey = "task-lock-" + task.getId();
                    if (redisService.acquireLock(lockKey, 30000)) {
                        try {
                            taskService.updateTaskStatus(task.getId(), TaskStatus.RUNNING);
                            taskExecutionService.createExecution(task.getId(), node.getId());
                            log.info("Scheduled task {} to node {}", task.getId(), node.getNodeId());
                        } finally {
                            redisService.releaseLock(lockKey);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error scheduling task {}:", task.getId(), e);
                }
            }
        }
    }

    private void checkTaskPreemption() {
        List<Task> runningTasks = taskService.getRunningTasks();
        List<Task> highPriorityWaiting = taskService.getTasksByStatus(TaskStatus.WAITING).stream()
                .filter(task -> task.getPriority() == TaskPriority.HIGH)
                .limit(5)
                .collect(Collectors.toList());

        for (Task highPriorityTask : highPriorityWaiting) {
            // 查找可以被抢占的低优先级任务
            Optional<Task> lowPriorityTask = runningTasks.stream()
                    .filter(task -> task.getPriority().ordinal() > highPriorityTask.getPriority().ordinal())
                    .findFirst();

            if (lowPriorityTask.isPresent()) {
                Task taskToPreempt = lowPriorityTask.get();
                log.info("Preempting low priority task {} for high priority task {}", 
                        taskToPreempt.getId(), highPriorityTask.getId());

                // 取消低优先级任务
                taskService.updateTaskStatus(taskToPreempt.getId(), TaskStatus.CANCELLED);
                List<TaskExecution> executions = taskExecutionService.getExecutionsByTaskId(taskToPreempt.getId());
                executions.forEach(exec -> 
                        taskExecutionService.updateExecutionStatus(exec.getExecutionId(), TaskStatus.CANCELLED)
                );

                // 重新调度高优先级任务
                String nodeId = consistentHashing.getNode(highPriorityTask.getId().toString());
                ExecutionNode node = nodeMap.get(nodeId);
                if (node != null) {
                    taskService.updateTaskStatus(highPriorityTask.getId(), TaskStatus.RUNNING);
                    taskExecutionService.createExecution(highPriorityTask.getId(), node.getId());
                    log.info("Scheduled high priority task {} to node {}", highPriorityTask.getId(), node.getNodeId());
                }
            }
        }
    }

    // 一致性哈希实现
    private static class ConsistentHashing {
        private final SortedMap<Integer, String> circle = new TreeMap<>();
        private final int replicas;

        public ConsistentHashing(int replicas) {
            this.replicas = replicas;
        }

        public void addNode(String nodeId) {
            for (int i = 0; i < replicas; i++) {
                int hash = getHash(nodeId + i);
                circle.put(hash, nodeId);
            }
        }

        public void removeNode(String nodeId) {
            for (int i = 0; i < replicas; i++) {
                int hash = getHash(nodeId + i);
                circle.remove(hash);
            }
        }

        public String getNode(String key) {
            if (circle.isEmpty()) {
                return null;
            }
            int hash = getHash(key);
            if (!circle.containsKey(hash)) {
                SortedMap<Integer, String> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            return circle.get(hash);
        }

        private int getHash(String key) {
            return key.hashCode();
        }
    }
}