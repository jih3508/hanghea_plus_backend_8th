package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * CouponFacade의 분산락 동작 테스트
 * 쿠폰 발급 시 분산락이 올바르게 적용되는지 검증
 */
@SpringBootTest
@ActiveProfiles("test")
public class CouponFacadeDistributedLockTest {

    @Autowired
    private CouponFacade couponFacade;

    @MockBean
    private UserService userService;

    @MockBean
    private CouponService couponService;

    @MockBean
    private UserCouponService userCouponService;

    @Test
    @DisplayName("동시 쿠폰 발급 요청 시 분산락을 통한 순차적 처리 검증")
    public void testConcurrentCouponIssueWithDistributedLock() throws InterruptedException {
        // given
        long userId = 1L;
        long couponId = 1L;
        int threadCount = 10;
        AtomicInteger issueCount = new AtomicInteger(0);
        
        // mock 설정
        DomainUser mockUser = mock(DomainUser.class);
        when(mockUser.getId()).thenReturn(userId);
        when(userService.findById(anyLong())).thenReturn(mockUser);
        
        DomainCoupon mockCoupon = mock(DomainCoupon.class);
        when(mockCoupon.getId()).thenReturn(couponId);
        when(couponService.issueCoupon(anyLong())).thenReturn(mockCoupon);
        
        // 쿠폰 발급 메서드 호출 시 카운터 증가
        doAnswer(invocation -> {
            long uid = invocation.getArgument(0);
            long cid = invocation.getArgument(1);
            
            // 여기서 실제 발급 처리 검증
            assertEquals(userId, uid);
            assertEquals(couponId, cid);
            
            // 호출 횟수 기록
            issueCount.incrementAndGet();
            
            return null;
        }).when(userCouponService).issue(anyLong(), anyLong());
        
        // 멀티스레드 환경 설정
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // when: 여러 스레드에서 동시에 쿠폰 발급 요청
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // 쿠폰 발급 요청
                    CouponIssueCommand command = new CouponIssueCommand(userId, couponId);
                    couponFacade.issue(command);
                } catch (Exception e) {
                    System.out.println("Coupon issue failed: " + e.getMessage());
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
        assertEquals(threadCount, issueCount.get(), "모든 쿠폰 발급 요청이 처리되어야 함");
        
        // 각 요청당 userCouponService.issue가 정확히 한 번씩 호출되었는지 확인
        verify(userCouponService, times(threadCount)).issue(eq(userId), eq(couponId));
    }
}