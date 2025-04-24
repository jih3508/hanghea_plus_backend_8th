package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.model.UpdatePoint;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("포인트 서비스 동시성 테스트")
class PointServiceConcurrencyTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceConcurrencyTest.class);
    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService executorService;
    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(10);
        latch = new CountDownLatch(1);

        // 테스트용 사용자 및 포인트 생성
        transactionTemplate.execute(status -> {
            // 사용자 생성
            CreateUser createUser = CreateUser.builder()
                    .name("포인트테스트유저")
                    .id("pointuser")
                    .build();
            User user = userRepository.create(createUser);

            // 포인트 생성 (또는 초기화)
            DomainPoint point = pointRepository.findByUserId(user.getId())
                    .orElseGet(() -> pointRepository.create(user.getId()));

            // 초기 포인트 설정 (10,000)
            pointRepository.update(UpdatePoint.builder()
                    .pointId(point.getId())
                    .point(new BigDecimal(10_000))
                    .build());

            return null;
        });
    }

    @Test
    @DisplayName("포인트 동시 충전 테스트")
    void 포인트_동시충전_테스트() throws InterruptedException {
        // given
        Long userId = 1L; // 테스트 사용자 ID

        // 시작 포인트 조회
        BigDecimal initialPoint = pointService.getPoint(userId).getPoint();

        // 10명의 사용자가 동시에 각 1,000 포인트씩 사용 시도
        int concurrentRequests = 10;
        BigDecimal useAmount = new BigDecimal(1_000);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);

        // when
        for (int i = 0; i < concurrentRequests; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    // 트랜잭션 내에서 포인트 사용 시도
                    transactionTemplate.execute(status -> {
                        pointService.charge(userId, useAmount);
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
        // 데이터베이스에서 최종 포인트 조회
        BigDecimal finalPoint = pointService.getPoint(userId).getPoint();

        // 예상 포인트 계산 (초기 포인트 - 성공한 요청 수 * 사용 금액)
        BigDecimal expectedPoint = initialPoint.add(new BigDecimal(successCount.get()).multiply(useAmount));

        // 결과 출력
        System.out.println("초기 포인트: " + initialPoint);
        System.out.println("성공한 요청 수: " + successCount.get());
        System.out.println("실패한 요청 수: " + failureCount.get());
        System.out.println("예상 포인트: " + expectedPoint);
        System.out.println("최종 포인트: " + finalPoint);

        // 최종 포인트는 예상 포인트와 일치해야 함
        assertThat(finalPoint).isEqualTo(expectedPoint);

        // 초기 포인트가 10,000이고 요청당 1,000씩 사용하므로, 최대 10개의 요청이 성공할 수 있음
        assertThat(successCount.get()).isLessThanOrEqualTo(10);

        // 총 요청 수는 concurrentRequests와 같아야 함
        assertThat(successCount.get() + failureCount.get()).isEqualTo(concurrentRequests);
    }

    @Test
    @DisplayName("낙관적 락 - 포인트 동시 차감 테스트")
    void 낙관적_락_포인트_동시차감_테스트() throws InterruptedException {
        // given
        Long userId = 1L; // 테스트 사용자 ID

        // 시작 포인트 조회
        BigDecimal initialPoint = pointService.getPoint(userId).getPoint();

        // 10명의 사용자가 동시에 각 1,000 포인트씩 사용 시도
        int concurrentRequests = 10;
        BigDecimal useAmount = new BigDecimal(1_000);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger optimisticLockFailureCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);

        // when
        for (int i = 0; i < concurrentRequests; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    // 트랜잭션 내에서 포인트 사용 시도
                    transactionTemplate.execute(status -> {
                        try {
                            pointService.use(userId, useAmount);
                            successCount.incrementAndGet();
                        } catch (ApiExceptionResponse e) {
                            failureCount.incrementAndGet();
                        } catch (ObjectOptimisticLockingFailureException e) {
                            optimisticLockFailureCount.incrementAndGet();
                        }
                        return null;
                    });

                } catch (Exception e) {
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
        // 데이터베이스에서 최종 포인트 조회
        BigDecimal finalPoint = pointService.getPoint(userId).getPoint();

        // 예상 포인트 계산 (초기 포인트 - 성공한 요청 수 * 사용 금액)
        BigDecimal expectedPoint = initialPoint.subtract(new BigDecimal(successCount.get()).multiply(useAmount));

        // 결과 출력
        System.out.println("초기 포인트: " + initialPoint);
        System.out.println("성공한 요청 수: " + successCount.get());
        System.out.println("실패한 요청 수: " + failureCount.get());
        System.out.println("낙관적 락 충돌 횟수: " + optimisticLockFailureCount.get());
        System.out.println("예상 포인트: " + expectedPoint);
        System.out.println("최종 포인트: " + finalPoint);

        // 최종 포인트는 예상 포인트와 일치해야 함
        assertThat(finalPoint).isEqualTo(expectedPoint);

        // 총 요청 수는 concurrentRequests와 같아야 함
        assertThat(successCount.get() + failureCount.get() + optimisticLockFailureCount.get()).isEqualTo(concurrentRequests);
    }

}