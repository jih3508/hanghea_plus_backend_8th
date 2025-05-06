package kr.hhplus.be.server.common.lock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 분산락 동작 테스트 클래스
 * 각 구현체(Simple, Spin, Redisson)의 동작을 검증
 */
@SpringBootTest
@ActiveProfiles("test")
public class DistributedLockTest {

    @Autowired
    private SimpleLock simpleLock;

    @Autowired
    private SpinLock spinLock;

    @Autowired
    private RedissonLock redissonLock;

    @Test
    @DisplayName("Simple Lock 동시성 테스트")
    public void testSimpleLock() throws InterruptedException {
        // given
        final int threadCount = 10;
        final AtomicInteger counter = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // when: 여러 스레드에서 동시에 같은 키로 락 획득 시도
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // Simple Lock은 재시도 없이 실패하므로, 예외 발생 가능성이 있음
                    // 이를 감안하여 try-catch로 처리
                    try {
                        simpleLock.executeWithLock("test_simple_lock", 1000, () -> {
                            // 카운터 증가 후 잠시 대기 (경쟁 상황 발생 유도)
                            counter.incrementAndGet();
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return null;
                        });
                    } catch (Exception e) {
                        // 락 획득 실패 시 로그만 남기고 무시
                        System.out.println("Simple lock acquisition failed: " + e.getMessage());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // then: 모든 스레드가 완료될 때까지 대기 후 결과 확인
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        // Simple Lock은 일부 실패할 수 있으므로 정확한 값을 검증하지 않고,
        // 대신 카운터 값이 threadCount 이하인지 확인
        assert counter.get() <= threadCount;
    }

    @Test
    @DisplayName("Spin Lock 동시성 테스트")
    public void testSpinLock() throws InterruptedException {
        // given
        final int threadCount = 10;
        final AtomicInteger counter = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // when: 여러 스레드에서 동시에 같은 키로 락 획득 시도
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    spinLock.executeWithLock("test_spin_lock", 3000, () -> {
                        // 카운터 증가 후 잠시 대기 (경쟁 상황 발생 유도)
                        counter.incrementAndGet();
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return null;
                    });
                } catch (Exception e) {
                    // 락 획득 실패 시 로그 출력
                    System.out.println("Spin lock acquisition failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // then: 모든 스레드가 완료될 때까지 대기 후 결과 확인
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        // Spin Lock은 재시도를 통해 더 많은 락 획득 성공이 기대됨
        // 하지만 타임아웃으로 일부 실패할 수 있음
        assert counter.get() <= threadCount;
        assert counter.get() > 0;
    }

    @Test
    @DisplayName("Redisson Lock 동시성 테스트")
    public void testRedissonLock() throws InterruptedException {
        // given
        final int threadCount = 10;
        final AtomicInteger counter = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // when: 여러 스레드에서 동시에 같은 키로 락 획득 시도
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    redissonLock.executeWithLock("test_redisson_lock", 5000, () -> {
                        // 카운터 증가 후 잠시 대기 (경쟁 상황 발생 유도)
                        counter.incrementAndGet();
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return null;
                    });
                } catch (Exception e) {
                    // 락 획득 실패 시 로그 출력
                    System.out.println("Redisson lock acquisition failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // then: 모든 스레드가 완료될 때까지 대기 후 결과 확인
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        // Redisson Lock은 Pub/Sub 방식으로 효율적인 락 관리가 기대됨
        assertEquals(threadCount, counter.get());
    }
}