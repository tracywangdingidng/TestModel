package com.taskscheduler.service;

import com.taskscheduler.enums.NodeStatus;
import com.taskscheduler.model.ExecutionNode;
import com.taskscheduler.repository.ExecutionNodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class ExecutionNodeService {

    @Autowired
    private ExecutionNodeRepository executionNodeRepository;

    public ExecutionNode registerNode(ExecutionNode node) {
        log.info("Registering execution node: {}", node.getNodeName());
        Optional<ExecutionNode> existingNode = executionNodeRepository.findByNodeId(node.getNodeId());
        if (existingNode.isPresent()) {
            ExecutionNode updatedNode = existingNode.get();
            updatedNode.setStatus(NodeStatus.ACTIVE);
            updatedNode.setLastHeartbeat(new Date());
            updatedNode.setIpAddress(node.getIpAddress());
            updatedNode.setPort(node.getPort());
            log.info("Node already exists, updating status: {}", node.getNodeId());
            return executionNodeRepository.save(updatedNode);
        } else {
            node.setStatus(NodeStatus.ACTIVE);
            node.setLastHeartbeat(new Date());
            ExecutionNode savedNode = executionNodeRepository.save(node);
            log.info("Node registered successfully: {}", savedNode.getNodeId());
            return savedNode;
        }
    }

    public void updateNodeHeartbeat(String nodeId) {
        log.info("Updating heartbeat for node: {}", nodeId);
        Optional<ExecutionNode> node = executionNodeRepository.findByNodeId(nodeId);
        node.ifPresent(n -> {
            n.setLastHeartbeat(new Date());
            n.setStatus(NodeStatus.ACTIVE);
            executionNodeRepository.save(n);
        });
    }

    public void updateNodeStatus(String nodeId, NodeStatus status) {
        log.info("Updating node status: {} to {}", nodeId, status);
        Optional<ExecutionNode> node = executionNodeRepository.findByNodeId(nodeId);
        node.ifPresent(n -> {
            n.setStatus(status);
            executionNodeRepository.save(n);
        });
    }

    /**
     * 获取所有节点
     * @return 所有节点列表
     */
    public List<ExecutionNode> getAllNodes() {
        return executionNodeRepository.findAll();
    }

    public List<ExecutionNode> getActiveNodes() {
        log.info("Getting active execution nodes");
        return executionNodeRepository.findByStatus(NodeStatus.ACTIVE);
    }

    public ExecutionNode getNodeById(String nodeId) {
        log.info("Getting node by id: {}", nodeId);
        return executionNodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found: " + nodeId));
    }

    public void updateNodeResources(String nodeId, double cpuUsage, double memoryUsage) {
        log.info("Updating node resources: {} CPU: {}% Memory: {}%", nodeId, cpuUsage, memoryUsage);
        Optional<ExecutionNode> node = executionNodeRepository.findByNodeId(nodeId);
        node.ifPresent(n -> {
            n.setCpuUsage(cpuUsage);
            n.setMemoryUsage(memoryUsage);
            executionNodeRepository.save(n);
        });
    }

    public long getActiveNodeCount() {
        log.info("Getting active node count");
        return executionNodeRepository.countByStatus(NodeStatus.ACTIVE);
    }

    public long countByStatus(NodeStatus status) {
        log.info("Counting nodes by status: {}", status);
        return executionNodeRepository.countByStatus(status);
    }
}