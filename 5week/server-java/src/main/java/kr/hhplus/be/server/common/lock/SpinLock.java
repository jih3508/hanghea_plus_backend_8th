package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis SETNX를 활용한 Spin Lock 구현체
 * 락 획득에 실패하면 재시도하는 방식으로 구현
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpinLock implements DistributedLock {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "spin_lock:";
    private static final long RETRY_DELAY_MS = 100;
    private static final int MAX_RETRIES = 30;
    
    @Override
    public <T> T executeWithLock(String key, long timeoutMillis, Supplier<T> supplier) {
        String lockKey = LOCK_PREFIX + key;
        boolean acquired = false;
        
        // 타임아웃 계산용 변수
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutMillis;
        int retryCount = 0;
        
        try {
            // 락 획득 시도 (Spin Lock 방식)
            while (!acquired && System.currentTimeMillis() < endTime && retryCount < MAX_RETRIES) {
                acquired = Boolean.TRUE.equals(redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, Thread.currentThread().getId(), timeoutMillis, TimeUnit.MILLISECONDS));
                
                if (!acquired) {
                    // 락 획득 실패 시 잠시 대기 후 재시도
                    retryCount++;
                    log.debug("Waiting for spin lock, key: {}, attempt: {}", key, retryCount);
                    
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Spin lock interrupted for key: " + key, e);
                    }
                }
            }
            
            if (!acquired) {
                throw new IllegalStateException("Failed to acquire Spin lock for key: " + key + " after " + retryCount + " attempts");
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