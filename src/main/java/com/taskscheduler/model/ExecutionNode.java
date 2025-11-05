package com.taskscheduler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskscheduler.enums.NodeStatus;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "execution_nodes")
@EntityListeners(AuditingEntityListener.class)
public class ExecutionNode implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_id", nullable = false, unique = true, length = 50)
    private String nodeId;

    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NodeStatus status = NodeStatus.ACTIVE;

    @Column(name = "cpu_usage", nullable = false)
    private Double cpuUsage = 0.0;

    @Column(name = "memory_usage", nullable = false)
    private Double memoryUsage = 0.0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_heartbeat", nullable = false)
    private Date lastHeartbeat;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_time", nullable = false, updatable = false)
    private Date createdTime;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_time", nullable = false)
    private Date updatedTime;
}