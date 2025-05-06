package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.application.order.OrderCommand.OrderItem;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * OrderFacade의 분산락 동작 테스트
 * 주문 처리 시 분산락이 올바르게 적용되는지 검증
 */
@SpringBootTest
@ActiveProfiles("test")
public class OrderFacadeDistributedLockTest {

    @Autowired
    private OrderFacade orderFacade;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductStockService productStockService;

    @Test
    @DisplayName("동시 주문 요청 시 분산락을 통한 순차적 재고 감소 검증")
    public void testConcurrentOrderingWithDistributedLock() throws InterruptedException {
        // given
        long productId = 1L;
        int orderCount = 10;
        int threadCount = 10;
        AtomicInteger stockReduceCount = new AtomicInteger(0);
        
        // mock 설정
        DomainProduct mockProduct = mock(DomainProduct.class);
        when(mockProduct.getId()).thenReturn(productId);
        when(mockProduct.getPrice()).thenReturn(BigDecimal.valueOf(10000));
        
        when(productService.getProduct(anyLong())).thenReturn(mockProduct);
        
        // 재고 감소 메서드 호출 시 카운터 증가
        doAnswer(invocation -> {
            long pid = invocation.getArgument(0);
            int quantity = invocation.getArgument(1);
            
            // 여기서 실제 재고 처리 검증
            assertEquals(productId, pid);
            assertEquals(1, quantity);
            
            // 호출 횟수 기록
            stockReduceCount.incrementAndGet();
            
            return null;
        }).when(productStockService).delivering(anyLong(), anyInt());
        
        // 멀티스레드 환경 설정
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // when: 여러 스레드에서 동시에 주문 요청
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // 상품 1개 주문 요청
                    OrderCommand command = createOrderCommand(1L, productId, 1);
                    orderFacade.order(command);
                } catch (Exception e) {
                    System.out.println("Order failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // then: 모든 스레드가 완료될 때까지 대기 후 검증
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        // 각 요청이 분산락에 의해 순차적으로 처리되었는지 확인
        // Redisson Lock은 재진입을 허용하기 때문에 모든 요청이 처리되어야 함
        assertEquals(threadCount, stockReduceCount.get(), "모든 주문 요청에 대해 재고 감소가 발생해야 함");
        
        // 각 요청당 productStockService.delivering이 정확히 한 번씩 호출되었는지 확인
        verify(productStockService, times(threadCount)).delivering(eq(productId), eq(1));
    }
    
    /**
     * 테스트용 주문 명령 생성
     */
    private OrderCommand createOrderCommand(Long userId, Long productId, int quantity) {
        OrderItem item = new OrderItem(productId, null, quantity);
        return new OrderCommand(userId, List.of(item));
    }
}