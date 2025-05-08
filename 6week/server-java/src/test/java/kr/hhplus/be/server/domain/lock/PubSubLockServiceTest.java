package kr.hhplus.be.server.domain.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class PubSubLockServiceTest {

    @Autowired
    private PubSubLockService pubSubLockService;

    private String testKey;
    private List<String> testKeys;

    @BeforeEach
    void setUp() {
        // 테스트마다 고유한 키를 생성하여 테스트 간 간섭 방지
        testKey = "test:lock:" + UUID.randomUUID();
        testKeys = Arrays.asList(
                "test:lock:" + UUID.randomUUID(),
                "test:lock:" + UUID.randomUUID(),
                "test:lock:" + UUID.randomUUID()
        );
    }

    @Test
    @DisplayName("단일 락 테스트")
    void 단일_락() {
        // given
        RuntimeException testException = new RuntimeException("Test exception");

        // when/then
        assertThatThrownBy(() -> {
            pubSubLockService.executeWithLock(
                    testKey, 1000L, 10000L, TimeUnit.MILLISECONDS, () -> {
                        throw testException;
                    }
            );
        }).isSameAs(testException);
    }

    @Test
    @DisplayName("다중 락 테스트")
    void 다중_락() throws Throwable {
        // given
        String result = "multiple locks result";

        // when
        String actual = pubSubLockService.executeWithLockList(
                testKeys, 1000L, 10000L, TimeUnit.MILLISECONDS, () -> result
        );

        // then
        assertThat(actual).isEqualTo(result);
    }

    @Test
    void 동시_락() throws InterruptedException {
        // given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pubSubLockService.executeWithLock(testKey, 1000L, 5000L, TimeUnit.MILLISECONDS, () -> {
                        try {
                            // 짧은 시간 슬립하여 다른 스레드가 락 획득을 시도하도록 함
                            Thread.sleep(100);
                            successCount.incrementAndGet();
                            return "success";
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return "interrupted";
                        }
                    });
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        latch.await();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        executorService.shutdown();
    }

    @Test
    void 동시_List_락() throws InterruptedException {
        // given
        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        // 각 스레드가 모든 락을 획득하려고 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    try {
                        pubSubLockService.executeWithLockList(testKeys, 5000L, 10000L, TimeUnit.MILLISECONDS, () -> {
                            try {
                                // 짧은 시간 슬립하여 다른 스레드가 락 획득을 시도하도록 함
                                Thread.sleep(100);
                                successCount.incrementAndGet();
                                return "success";
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return "interrupted";
                            }
                        });
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        latch.await();

        assertThat(successCount.get()).isEqualTo(1);

        executorService.shutdown();
    }

    @Test
    void 락_획득_실패() throws InterruptedException {
        // given
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch testCompleted = new CountDownLatch(1);

        Thread lockHolder = new Thread(() -> {
            try {
                pubSubLockService.executeWithLock(testKey, 1000L, 30000L, TimeUnit.MILLISECONDS, () -> {
                    lockAcquired.countDown();
                    try {
                        // 테스트가 완료될 때까지 락 유지
                        testCompleted.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "locked";
                });
            } catch (Throwable e) {
                // 무시
            }
        });

        lockHolder.start();

        // 첫 번째 스레드가 락을 획득할 때까지 대기
        boolean acquired = lockAcquired.await(5, TimeUnit.SECONDS);
        assertThat(acquired).isTrue();

        // when/then - 두 번째 시도는 예외와 함께 실패해야 함
        assertThatThrownBy(() -> {
            pubSubLockService.executeWithLock(testKey, 1000L, 5000L, TimeUnit.MILLISECONDS, () -> "should not reach here");
        })
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("락 획득 실패");

        // 락 홀더 스레드 해제
        testCompleted.countDown();
        lockHolder.join(5000);
    }



}