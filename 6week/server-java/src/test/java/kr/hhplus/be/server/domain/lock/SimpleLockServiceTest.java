package kr.hhplus.be.server.domain.lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class SimpleLockServiceTest {

    @Autowired
    private SimpleLockService lockService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private String testLockKey;


    @BeforeEach
    void setUp() {
        testLockKey = "test:lock:" + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 키 삭제
        redisTemplate.delete(testLockKey);
    }


    @Test
    @DisplayName("심플락 획득 테스트")
    void 락_획득(){
        // Arrange
        String expectedResult = "Execution successful";
        Supplier<String> supplier = () -> expectedResult;

        // Act
        String result = lockService.executeWithLock(testLockKey, 1L, 10L, TimeUnit.SECONDS, supplier);

        // Assert
        assertThat(expectedResult).isEqualTo(expectedResult);
        // 락이 해제되었는지 확인
        assertThat(redisTemplate.opsForValue().get(testLockKey)).isNull();
    }

    @Test
    @DisplayName("여러 스레드에서 동시에 락 획득 시도 시 하나만 성공하는지 확인")
    void 다수_스레드_테스트() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 각 스레드에서 실행할 작업
        Runnable task = () -> {
            try {
                lockService.executeWithLock(testLockKey, 1L, 5L, TimeUnit.SECONDS, () -> {
                    try {
                        // 스레드 실행 시간을 조금 주어 경합 상황 만들기
                        Thread.sleep(100);
                        successCount.incrementAndGet();
                        return "Success";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return "Interrupted";
                    }
                });
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        };

        // Act
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(task);
        }

        // 모든 스레드가 작업을 마칠 때까지 대기
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Assert
        // 하나의 스레드만 성공하고 나머지는 락 획득 실패
        assertThat(1).isEqualTo(successCount.get());
        assertThat(threadCount -1).isNotEqualTo(successCount.get());
    }



}