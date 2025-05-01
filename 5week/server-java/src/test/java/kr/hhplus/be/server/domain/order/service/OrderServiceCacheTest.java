package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.common.cache.CacheService;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class OrderServiceCacheTest {

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
    private CacheService cacheService;

    @MockitoBean
    private OrderProductHistoryRepository historyRepository;

    @Test
    void shouldCacheOrderHistoryData() {
        // given
        List<OrderHistoryProductGroupVo> expectedData = Arrays.asList(
                new OrderHistoryProductGroupVo(1L, 5),
                new OrderHistoryProductGroupVo(2L, 3)
        );

        when(historyRepository.findGroupByProductIdThreeDays()).thenReturn(expectedData);

        // when - 첫 번째 호출
        List<OrderHistoryProductGroupVo> firstCall = orderService.threeDaysOrderProductHistory();

        // then - 결과 확인 및 저장소 호출 확인
        assertThat(firstCall).isEqualTo(expectedData);
        verify(historyRepository, times(1)).findGroupByProductIdThreeDays();

        // when - 두 번째 호출 (캐시에서 가져와야 함)
        List<OrderHistoryProductGroupVo> secondCall = orderService.threeDaysOrderProductHistory();

        // then - 결과 확인 및 저장소 추가 호출 없음을 확인
        assertThat(secondCall).isEqualTo(expectedData);
        verify(historyRepository, times(1)).findGroupByProductIdThreeDays(); // 추가 호출 없음
    }

    @Test
    void programmaticCacheShouldWorkCorrectly() {
        // given
        String cacheKey = "test:cache:key";
        String expectedValue = "cached-value";

        // when - 첫 번째 호출 (캐시에 저장)
        String firstResult = cacheService.getOrCreate(cacheKey, 60, () -> {
            // 이 코드는 처음 한 번만 실행되어야 함
            return expectedValue;
        });

        // then
        assertThat(firstResult).isEqualTo(expectedValue);
        assertThat(cacheService.exists(cacheKey)).isTrue();

        // when - 두 번째 호출 (캐시에서 반환)
        AtomicBoolean supplierExecuted = new AtomicBoolean(false);
        String secondResult = cacheService.getOrCreate(cacheKey, 60, () -> {
            // 이 코드는 실행되지 않아야 함 (캐시에서 가져오므로)
            supplierExecuted.set(true);
            return "different-value";
        });

        // then
        assertThat(secondResult).isEqualTo(expectedValue); // 캐시된 값이 반환되어야 함
        assertThat(supplierExecuted.get()).isFalse(); // 서플라이어가 실행되지 않아야 함

        // when - 캐시 무효화 후 호출
        cacheService.invalidate(cacheKey);
        String thirdResult = cacheService.getOrCreate(cacheKey, 60, () -> "new-value");

        // then
        assertThat(thirdResult).isEqualTo("new-value"); // 새로운 값이 반환되어야 함
    }
}
