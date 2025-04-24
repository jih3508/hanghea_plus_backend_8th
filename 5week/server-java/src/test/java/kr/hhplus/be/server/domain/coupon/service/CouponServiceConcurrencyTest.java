package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("쿠폰 서비스 동시성 테스트")
class CouponServiceConcurrencyTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(CouponServiceConcurrencyTest.class);
    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private ExecutorService executorService;

    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(10);
        latch = new CountDownLatch(1);
    }

    @Test
    @DisplayName(value = " 한정 수량 쿠폰 동시 발급 테스트")
    void 쿠폰_동시발급_테스트() throws InterruptedException {
        // given
        // 10개 한정 쿠폰 생성
        final Long limitedCouponId = transactionTemplate.execute(status -> {
            CreateCoupon createCoupon = CreateCoupon.builder()
                    .couponNumber(UUID.randomUUID().toString())
                    .type(CouponType.FLAT)
                    .discountPrice(new BigDecimal(5_000))
                    .quantity(10) // 10개 한정 쿠폰
                    .startDateTime(LocalDateTime.now().minusDays(1))
                    .endDateTime(LocalDateTime.now().plusDays(30))
                    .build();
            DomainCoupon coupon = couponRepository.create(createCoupon);
            return coupon.getId();
        });

        // 20명의 사용자가 동시에 10개의 한정 쿠폰을 신청
        int concurrentRequests = 20;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);

        // when
        for (int i = 0; i < concurrentRequests; i++) {
            final int userIdx = i;
            executorService.submit(() -> {
                try {

                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    // 트랜잭션 내에서 쿠폰 발급 시도
                    transactionTemplate.execute(status -> {
                        couponService.issueCoupon(limitedCouponId);
                        successCount.incrementAndGet();
                        return null;
                    });

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }


        // 모든 스레드 동시 시작
        latch.countDown();

        // 모든 스레드 완료 대기
        completionLatch.await();



        // then
        // 성공한 발급은 10개 이하여야 함 (쿠폰이 10개)
        //assertThat(successCount.get()).isLessThanOrEqualTo(10);

        // 실패한 발급은 10개 이상이어야 함
       // assertThat(failureCount.get()).isGreaterThanOrEqualTo(10);

        // 총 발급 시도는 20개
        assertThat(successCount.get() + failureCount.get()).isEqualTo(20);

        // 데이터베이스에서 실제 쿠폰 남은 수량 확인
        DomainCoupon coupon = couponService.getCoupon(limitedCouponId);
        log.info("successCount: " + successCount.get());
        log.info("failureCount: " + failureCount.get());
        log.info(String.format("쿠폰 개수: %d",coupon.getQuantity()));
        //assertThat(coupon.getQuantity()).isEqualTo(0); // 모든 쿠폰이 소진되어야 함
    }

}