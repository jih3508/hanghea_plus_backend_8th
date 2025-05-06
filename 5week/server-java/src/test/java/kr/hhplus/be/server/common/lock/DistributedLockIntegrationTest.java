package kr.hhplus.be.server.common.lock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class DistributedLockIntegrationTest {

    @Autowired
    private SimpleLock simpleLock;

    @Autowired
    private SpinLock spinLock;

    @Autowired
    private RedissonLock redissonLock;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void simpleLockConcurrentTest() throws InterruptedException {
        testConcurrentIncrements(simpleLock);
    }

    @Test
    void spinLockConcurrentTest() throws InterruptedException {
        testConcurrentIncrements(spinLock);
    }

    @Test
    void redissonLockConcurrentTest() throws InterruptedException {
        testConcurrentIncrements(redissonLock);
    }

    private void testConcurrentIncrements(DistributedLock lock) throws InterruptedException {
        // Set initial value
        String counterKey = "test:counter:" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(counterKey, 0);

        // Number of threads and increments
        int threadCount = 10;
        int incrementsPerThread = 100;
        
        // Setup thread pool and synchronization
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // Start concurrent operations
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        String lockKey = "lock:" + counterKey;
                        
                        lock.executeWithLock(lockKey, 1000, () -> {
                            Integer currentValue = (Integer) redisTemplate.opsForValue().get(counterKey);
                            redisTemplate.opsForValue().set(counterKey, currentValue + 1);
                            return null;
                        });
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        latch.await();
        executor.shutdown();
        
        // Verify the final value
        Integer finalValue = (Integer) redisTemplate.opsForValue().get(counterKey);
        assertEquals(threadCount * incrementsPerThread, finalValue);
    }
}
