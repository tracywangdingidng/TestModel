package com.taskscheduler.enums;

public enum TaskStatus {
    WAITING,      // 等待执行
    RUNNING,      // 执行中
    COMPLETED,    // 执行完成
    FAILED,       // 执行失败
    PAUSED,       // 任务暂停
    CANCELLED     // 任务取消
}