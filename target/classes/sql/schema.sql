-- Create Database
CREATE DATABASE IF NOT EXISTS task_scheduler DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE task_scheduler;

-- Task Table
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL,
    task_type ENUM('INSTANT', 'SCHEDULED', 'PERIODIC') NOT NULL,
    priority ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM',
    cron_expression VARCHAR(50),
    period INT,
    period_unit ENUM('SECONDS', 'MINUTES', 'HOURS', 'DAYS'),
    task_content TEXT NOT NULL,
    status ENUM('WAITING', 'RUNNING', 'COMPLETED', 'FAILED', 'PAUSED', 'CANCELLED') DEFAULT 'WAITING',
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    retry_interval INT DEFAULT 60,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    FOREIGN KEY (parent_id) REFERENCES tasks(id)
);

-- Task Dependencies Table
CREATE TABLE IF NOT EXISTS task_dependencies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    dependency_task_id BIGINT NOT NULL,
    dependency_type ENUM('PREDECESSOR', 'SUCCESSOR') DEFAULT 'PREDECESSOR',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (dependency_task_id) REFERENCES tasks(id),
    UNIQUE KEY uk_task_dependency (task_id, dependency_task_id)
);

-- Execution Nodes Table
CREATE TABLE IF NOT EXISTS execution_nodes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    node_id VARCHAR(50) NOT NULL UNIQUE,
    node_name VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    port INT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'FAILED') DEFAULT 'ACTIVE',
    cpu_usage DOUBLE DEFAULT 0.0,
    memory_usage DOUBLE DEFAULT 0.0,
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Task Executions Table
CREATE TABLE IF NOT EXISTS task_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    execution_node_id BIGINT NOT NULL,
    execution_id VARCHAR(100) NOT NULL UNIQUE,
    status ENUM('WAITING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'WAITING',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration BIGINT,
    retry_count INT DEFAULT 0,
    error_message TEXT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (execution_node_id) REFERENCES execution_nodes(id)
);

-- Task Results Table
CREATE TABLE IF NOT EXISTS task_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id VARCHAR(100) NOT NULL UNIQUE,
    task_id BIGINT NOT NULL,
    result_data TEXT,
    result_status ENUM('SUCCESS', 'FAILURE') NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES task_executions(execution_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id)
);

-- Task Logs Table
CREATE TABLE IF NOT EXISTS task_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id VARCHAR(100) NOT NULL,
    log_level ENUM('INFO', 'WARN', 'ERROR', 'DEBUG') DEFAULT 'INFO',
    log_message TEXT NOT NULL,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES task_executions(execution_id)
);