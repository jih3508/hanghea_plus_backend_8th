package kr.hhplus.be.server.domain.lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
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
class SpinLockServiceTest {

    private static final Logger log = LoggerFactory.getLogger(SpinLockServiceTest.class);
    @Autowired
    private SpinLockService lockService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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

    @AfterEach
    void tearDown() {
        // 테스트 후 생성된 키 삭제
        redisTemplate.delete(testKey);
        redisTemplate.delete(testKeys);
    }

    @Test
    @DisplayName(" 락 획득 및 Supplier 실행 기능을 검증")
    void 락_획득() {
        // given
        String result = "test result";

        // when
        String actual = lockService.executeWithLock(
                testKey, 1000L, 10000L, TimeUnit.MILLISECONDS, () -> result
        );

        // then
        assertThat(actual).isEqualTo(result);
    }

    @Test
    @DisplayName("락 획득 시도 횟수 초과 테스트")
    void 락_획득_시도_초과() {
        // given
        redisTemplate.opsForValue().set(testKey, "other-thread", 30, TimeUnit.SECONDS);

        // when/then
        assertThatThrownBy(() -> lockService.executeWithLock(testKey, 1000L, 10000L, TimeUnit.MILLISECONDS, () -> "result"
        )).hasMessageContaining("락 획득 시도 횟수 초과");

    }

    @Test
    @DisplayName("여러 스레드에서 동시에 같은 키에 접근할 때 동시성 제어가 제대로 작동하는지 검증")
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
                lockService.executeWithLock(testKey, 1L, 5L, TimeUnit.SECONDS, () -> {
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

        //then
        log.info("success count: {}", successCount.get());
        log.info("fail count: {}", failCount.get());
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

    }


    @Test
    void 디중_락() {
        // given
        String result = "multiple locks result";

        // when
        String actual = lockService.executeWithLockList(
                testKeys, 1000L, 10000L, TimeUnit.MILLISECONDS, () -> result
        );

        // then
        assertThat(actual).isEqualTo(result);
    }

}