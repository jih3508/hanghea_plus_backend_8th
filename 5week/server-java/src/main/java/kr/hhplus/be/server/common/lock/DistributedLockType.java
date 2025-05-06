package kr.hhplus.be.server.common.lock;

/**
 * Enum for different types of distributed locks
 */
public enum DistributedLockType {
    SIMPLE,     // Simple lock using Redis SETNX
    SPIN,       // Lock with spin retry
    REDISSON    // Redisson-based pub/sub lock
}
