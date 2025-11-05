package com.taskscheduler.repository;

import com.taskscheduler.enums.NodeStatus;
import com.taskscheduler.model.ExecutionNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExecutionNodeRepository extends JpaRepository<ExecutionNode, Long> {
    Optional<ExecutionNode> findByNodeId(String nodeId);
    List<ExecutionNode> findByStatus(NodeStatus status);
    long countByStatus(NodeStatus status);
}