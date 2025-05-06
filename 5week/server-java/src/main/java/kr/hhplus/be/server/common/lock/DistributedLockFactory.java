package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for creating appropriate distributed lock instances
 */
@Component
@RequiredArgsConstructor
public class DistributedLockFactory {
    
    private final SimpleLock simpleLock;
    private final SpinLock spinLock;
    private final RedissonLock redissonLock;
    
    @Value("${app.lock.type:REDISSON}")
    private DistributedLockType defaultLockType;
    
    /**
     * Get the default distributed lock implementation
     * @return Default distributed lock instance
     */
    public DistributedLock getLock() {
        return getLock(defaultLockType);
    }
    
    /**
     * Get a specific distributed lock implementation
     * @param lockType Type of lock to get
     * @return Distributed lock instance
     */
    public DistributedLock getLock(DistributedLockType lockType) {
        return switch (lockType) {
            case SIMPLE -> simpleLock;
            case SPIN -> spinLock;
            case REDISSON -> redissonLock;
        };
    }
}
