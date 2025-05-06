package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis SETNX를 활용한 Simple Lock 구현체
 * 단순하게 락을 획득하고 실패하면 예외 발생
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SimpleLock implements DistributedLock {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "simple_lock:";
    
    @Override
    public <T> T executeWithLock(String key, long timeoutMillis, Supplier<T> supplier) {
        String lockKey = LOCK_PREFIX + key;
        boolean acquired = false;
        
        try {
            // SETNX 명령어로 락 획득 시도 (값이 있으면 실패, 없으면 성공)
            acquired = Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, Thread.currentThread().getId(), timeoutMillis, TimeUnit.MILLISECONDS));
            
            if (!acquired) {
                throw new IllegalStateException("Failed to acquire Simple lock for key: " + key);
            }
            
            // 락 획득 성공 시 로직 실행
            return supplier.get();
        } finally {
            // 락 해제
            if (acquired) {
                redisTemplate.delete(lockKey);
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