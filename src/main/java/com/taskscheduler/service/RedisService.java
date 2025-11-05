package com.taskscheduler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public boolean acquireLock(String lockKey, long expireTime) {
        return acquireLock(lockKey, expireTime, 3, 1000);
    }

    public boolean acquireLock(String lockKey, long expireTime, int retryTimes, long retryInterval) {
        try {
            String lockValue = "locked";
            for (int i = 0; i < retryTimes; i++) {
                Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.MILLISECONDS);
                if (success != null && success) {
                    log.info("Acquired lock: {}", lockKey);
                    return true;
                }
                log.debug("Failed to acquire lock: {}, retry {}/{}", lockKey, i + 1, retryTimes);
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(retryInterval));
            }
        } catch (Exception e) {
            log.error("Error acquiring lock: {}", lockKey, e);
        }
        log.warn("Could not acquire lock after {} attempts: {}", retryTimes, lockKey);
        return false;
    }

    public void releaseLock(String lockKey) {
        try {
            redisTemplate.delete(lockKey);
            log.info("Released lock: {}", lockKey);
        } catch (Exception e) {
            log.error("Error releasing lock: {}", lockKey, e);
        }
    }

    public void set(String key, String value, long expireTime) {
        try {
            redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.MILLISECONDS);
            log.debug("Set redis key: {} with value: {}", key, value);
        } catch (Exception e) {
            log.error("Error setting redis key: {}", key, e);
        }
    }

    public String get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            log.debug("Get redis key: {} with value: {}", key, value);
            return value;
        } catch (Exception e) {
            log.error("Error getting redis key: {}", key, e);
            return null;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted redis key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting redis key: {}", key, e);
        }
    }

    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error checking redis key existence: {}", key, e);
            return false;
        }
    }

    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            log.debug("Increment redis key: {} by {} result: {}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Error incrementing redis key: {}", key, e);
            return 0;
        }
    }
}
