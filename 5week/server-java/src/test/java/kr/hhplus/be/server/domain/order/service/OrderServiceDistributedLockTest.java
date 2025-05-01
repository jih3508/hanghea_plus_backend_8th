package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.common.lock.LockService;
import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class OrderServiceDistributedLockTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private LockService lockService;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void concurrentOrderCreationWithLock() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        Long userId = 1L;

        // when - 동일한 사용자에 대해 동시에 10개의 주문 생성 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    CreateOrder createOrder = CreateOrder.builder()
                            .userId(userId)
                            .totalPrice(BigDecimal.valueOf(1000))
                            .discountPrice(BigDecimal.ZERO)
                            .build();
                    
                    DomainOrder order = orderService.create(createOrder);
                    if (order != null && order.getId() != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // 락 획득 실패나 다른 예외 발생 시 로그
                    System.err.println("Order creation failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 작업을 완료할 때까지 대기
        latch.await();
        executorService.shutdown();

        // then
        // 모든 요청이 성공해야 함 (락을 통해 순차 처리되므로)
        assertThat(successCount.get()).isEqualTo(threadCount);
        
        // 사용자의 주문 조회하여 중복 주문이 없는지 확인
        List<DomainOrder> userOrders = orderService.getUserOrders(userId);
        assertThat(userOrders).hasSize(threadCount);
    }
    
    @Test
    void distributedLockShouldPreventConcurrentExecution() throws InterruptedException {
        // given
        String lockKey = "test:lock:key";
        int threadCount = 5;
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Integer> executionResults = new ArrayList<>();
        
        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    lockService.executeWithLock(lockKey, 5, 3, () -> {
                        // 임계 영역 - 한 번에 하나의 스레드만 실행 가능
                        int current = counter.getAndIncrement();
                        try {
                            // 스레드 간섭을 시뮬레이션하기 위한 짧은 대기
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        executionResults.add(current);
                        return null;
                    });
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executorService.shutdown();
        
        // then
        // 각 스레드가 임계 영역을 순차적으로 실행했다면 결과는 0,1,2,3,4 순서
        assertThat(executionResults).hasSize(threadCount);
        for (int i = 0; i < threadCount; i++) {
            assertThat(executionResults.get(i)).isEqualTo(i);
        }
    }
}
