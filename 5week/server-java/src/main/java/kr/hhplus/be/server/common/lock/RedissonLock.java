package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson-based Distributed Lock Implementation
 * Uses Redisson's Pub/Sub mechanism for lock management
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonLock implements DistributedLock {
    
    private final RedissonClient redissonClient;
    private static final String LOCK_PREFIX = "redisson_lock:";
    
    @Override
    public <T> T executeWithLock(String key, long timeoutMillis, Supplier<T> supplier) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        
        try {
            acquired = lock.tryLock(timeoutMillis, timeoutMillis, TimeUnit.MILLISECONDS);
            if (acquired) {
                return supplier.get();
            } else {
                throw new IllegalStateException("Failed to acquire lock for key: " + key);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Lock acquisition interrupted for key: " + key, e);
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }
    
    @Override
    public void executeWithLock(String key, long timeoutMillis, Runnable runnable) {
        executeWithLock(key, timeoutMillis, () -> {
            runnable.run();
            return null;
        });
    }
}
