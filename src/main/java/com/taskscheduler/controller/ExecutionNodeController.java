package com.taskscheduler.controller;

import com.taskscheduler.enums.NodeStatus;
import com.taskscheduler.model.ExecutionNode;
import com.taskscheduler.service.ExecutionNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
public class ExecutionNodeController {

    @Autowired
    private ExecutionNodeService executionNodeService;

    @PostMapping("/register")
    public ResponseEntity<ExecutionNode> registerNode(@RequestBody ExecutionNode node) {
        ExecutionNode registeredNode = executionNodeService.registerNode(node);
        return new ResponseEntity<>(registeredNode, HttpStatus.CREATED);
    }

    @PutMapping("/{nodeId}/heartbeat")
    public ResponseEntity<Void> updateHeartbeat(@PathVariable String nodeId) {
        executionNodeService.updateNodeHeartbeat(nodeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{nodeId}/status/{status}")
    public ResponseEntity<Void> updateNodeStatus(@PathVariable String nodeId, @PathVariable NodeStatus status) {
        executionNodeService.updateNodeStatus(nodeId, status);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{nodeId}/resources")
    public ResponseEntity<Void> updateNodeResources(@PathVariable String nodeId, 
                                                   @RequestParam double cpuUsage, 
                                                   @RequestParam double memoryUsage) {
        executionNodeService.updateNodeResources(nodeId, cpuUsage, memoryUsage);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ExecutionNode>> getAllNodes() {
        List<ExecutionNode> nodes = executionNodeService.getActiveNodes();
        return new ResponseEntity<>(nodes, HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ExecutionNode>> getActiveNodes() {
        List<ExecutionNode> nodes = executionNodeService.getActiveNodes();
        return new ResponseEntity<>(nodes, HttpStatus.OK);
    }

    @GetMapping("/{nodeId}")
    public ResponseEntity<ExecutionNode> getNode(@PathVariable String nodeId) {
        ExecutionNode node = executionNodeService.getNodeById(nodeId);
        return new ResponseEntity<>(node, HttpStatus.OK);
    }
}
