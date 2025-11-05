package com.taskscheduler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "task_logs")
@EntityListeners(AuditingEntityListener.class)
public class TaskLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_id", nullable = false, length = 100)
    private String executionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", nullable = false)
    private LogLevel logLevel = LogLevel.INFO;

    @Column(name = "log_message", nullable = false, columnDefinition = "TEXT")
    private String logMessage;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "log_time", nullable = false, updatable = false)
    private Date logTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", insertable = false, updatable = false)
    private TaskExecution taskExecution;

    public enum LogLevel {
        INFO, WARN, ERROR, DEBUG
    }
}