package com.taskscheduler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskscheduler.enums.TaskPriority;
import com.taskscheduler.enums.TaskStatus;
import com.taskscheduler.enums.TaskType;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "cron_expression", length = 50)
    private String cronExpression;

    @Column(name = "period")
    private Integer period;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_unit")
    private PeriodUnit periodUnit;

    @Column(name = "task_content", nullable = false, columnDefinition = "TEXT")
    private String taskContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.WAITING;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    @Column(name = "retry_interval")
    private Integer retryInterval = 60;

    @Column(name = "parent_id")
    private Long parentId;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_time", nullable = false, updatable = false)
    private Date createdTime;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_time", nullable = false)
    private Date updatedTime;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    public enum PeriodUnit {
        SECONDS, MINUTES, HOURS, DAYS
    }
}