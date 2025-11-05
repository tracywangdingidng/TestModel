package com.taskscheduler.service;

import com.taskscheduler.model.Task;
import com.taskscheduler.model.TaskExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendTaskStatusUpdate(Task task) {
        messagingTemplate.convertAndSend("/topic/task/status/" + task.getId(), task);
        messagingTemplate.convertAndSend("/topic/task/status/all", task);
    }

    public void sendTaskExecutionUpdate(TaskExecution execution) {
        messagingTemplate.convertAndSend("/topic/execution/status/" + execution.getExecutionId(), execution);
        messagingTemplate.convertAndSend("/topic/execution/status/task/" + execution.getTaskId(), execution);
    }

    public void sendTaskResultUpdate(String executionId, Object result) {
        messagingTemplate.convertAndSend("/topic/execution/result/" + executionId, result);
    }

    public void sendSystemStats(Object stats) {
        messagingTemplate.convertAndSend("/topic/system/stats", stats);
    }

    public void sendNodeStatusUpdate(Object node) {
        messagingTemplate.convertAndSend("/topic/node/status/all", node);
    }
}