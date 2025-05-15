package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.application.point.PointChargeCommand;
import kr.hhplus.be.server.application.point.PointFacade;
import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.model.DomainPointHistory;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.point.entity.PointTransactionType;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DisplayName("포인트 파사드 동시성 테스트")
class PointFacadeConcurrencyTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PointFacadeConcurrencyTest.class);

    @Autowired
    private PointFacade facade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointService pointService;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PointRepository pointRepository;


    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService executorService;
    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        // 동시 실행을 위한 스레드풀 및 래치 설정
        executorService = Executors.newFixedThreadPool(10);
        latch = new CountDownLatch(1);

        // 테스트용 사용자 생성
        transactionTemplate.execute(status -> {
            // 일반 사용자 생성
            CreateUser createUser = CreateUser.builder()
                    .name("일반사용자")
                    .id("normaluser")
                    .build();
            User user = userRepository.create(createUser);

            // 초기 포인트 설정
            DomainPoint point = DomainPoint.builder()
                    .id(user.getId())
                    .userId(user.getId())
                    .point(new BigDecimal(1000))  // 초기 포인트 1,000원
                    .build();
            pointRepository.save(point);

            // 한도 근접 사용자 생성
            createUser = CreateUser.builder()
                    .name("한도근접사용자")
                    .id("limituser")
                    .build();
            User limitUser = userRepository.create(createUser);

            // 한도에 근접한 초기 포인트 설정
            DomainPoint limitPoint = DomainPoint.builder()
                    .id(limitUser.getId())
                    .userId(limitUser.getId())
                    .point(new BigDecimal(999_000_000))  // 한도(10억)에 근접한 포인트
                    .build();
            pointRepository.save(limitPoint);

            return null;
        });
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    @DisplayName("동시에 여러번 충전 시 충전 합계 정확성 테스트")
    void 동시_충전_합계_정확성_테스트() throws InterruptedException {
        // given
        Long userId = 1L; // 일반 사용자
        BigDecimal initialPoint = facade.getPoint(userId);

        // 동시에 10번의 1,000원 충전 실행 (총 10,000원)
        int concurrentCharges = 10;
        BigDecimal chargeAmount = new BigDecimal(1000);
        BigDecimal expectedTotal = initialPoint.add(chargeAmount.multiply(new BigDecimal(concurrentCharges)));

        CountDownLatch completionLatch = new CountDownLatch(concurrentCharges);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < concurrentCharges; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    PointChargeCommand command = PointChargeCommand.builder()
                            .userID(userId)
                            .amount(chargeAmount)
                            .build();

                    facade.charge(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("충전 실패: {}", e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // 모든 스레드 동시 시작
        latch.countDown();

        // 모든 스레드 완료 대기
        completionLatch.await(10, TimeUnit.SECONDS);

        // then
        log.info("성공한 충전 수: {}, 실패한 충전 수: {}", successCount.get(), failureCount.get());

        // 모든 충전이 성공해야 함
        assertThat(successCount.get()).isEqualTo(concurrentCharges);
        assertThat(failureCount.get()).isZero();

        // 최종 포인트가 예상 금액과 일치해야 함
        BigDecimal finalPoint = facade.getPoint(userId);
        assertThat(finalPoint).isEqualTo(expectedTotal);

        // 충전 이력이 정확히 10개 생성되었는지 확인
        List<DomainPointHistory> histories = pointHistoryRepository.findByUserId(userId);
        long chargeHistories = histories.stream()
                .filter(h -> h.getType() == PointTransactionType.CHARGE)
                .count();
        assertThat(chargeHistories).isEqualTo(concurrentCharges);
    }

    @Test
    @DisplayName("한도 초과 동시성 테스트")
    void 한도_초과_동시성_테스트() throws InterruptedException {
        // given
        Long userId = 2L; // 한도 근접 사용자
        BigDecimal initialPoint = facade.getPoint(userId);

        // 각 충전 금액 (여러 금액으로 테스트)
        BigDecimal[] chargeAmounts = {
                new BigDecimal(500_000),      // 소액
                new BigDecimal(5_000_000),    // 중간 금액
                new BigDecimal(10_000_000)    // 대액
        };

        int concurrentCharges = chargeAmounts.length;
        CountDownLatch completionLatch = new CountDownLatch(concurrentCharges);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicReference<BigDecimal> lastSuccessAmount = new AtomicReference<>(BigDecimal.ZERO);

        // when
        for (int i = 0; i < concurrentCharges; i++) {
            final BigDecimal amount = chargeAmounts[i];
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    PointChargeCommand command = PointChargeCommand.builder()
                            .userID(userId)
                            .amount(amount)
                            .build();

                    BigDecimal result = facade.charge(command);
                    successCount.incrementAndGet();
                    lastSuccessAmount.set(amount);
                    log.info("충전 성공: 금액 {}, 결과 포인트 {}", amount, result);
                } catch (Exception e) {
                    log.error("충전 실패: 금액 {}, 오류 {}", amount, e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // 모든 스레드 동시 시작
        latch.countDown();

        // 모든 스레드 완료 대기
        completionLatch.await(10, TimeUnit.SECONDS);

        // then
        log.info("성공한 충전 수: {}, 실패한 충전 수: {}", successCount.get(), failureCount.get());

        // 일부는 성공하고 일부는 실패해야 함 (한도 초과로 인해)
        assertThat(successCount.get()).isGreaterThan(0);
        assertThat(failureCount.get()).isGreaterThan(0);
        assertThat(successCount.get() + failureCount.get()).isEqualTo(concurrentCharges);

        // 최종 포인트가 한도(10억)를 초과하지 않아야 함
        BigDecimal finalPoint = facade.getPoint(userId);
        assertThat(finalPoint).isLessThanOrEqualTo(new BigDecimal(1_000_000_000L));

        // 충전 이력 확인
        List<DomainPointHistory> histories = pointHistoryRepository.findByUserId(userId);
        long chargeHistories = histories.stream()
                .filter(h -> h.getType() == PointTransactionType.CHARGE)
                .count();
        assertThat(chargeHistories).isEqualTo(successCount.get());
    }


    @Test
    @DisplayName("충전과 사용 동시성 테스트")
    void 충전과_사용_동시성_테스트() throws InterruptedException {
        // given
        Long userId = 1L; // 일반 사용자
        BigDecimal initialPoint = facade.getPoint(userId);

        int totalOperations = 20;
        CountDownLatch completionLatch = new CountDownLatch(totalOperations);
        AtomicInteger chargeSuccessCount = new AtomicInteger(0);
        AtomicInteger useSuccessCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 충전 총액과 사용 총액 추적
        AtomicReference<BigDecimal> totalCharged = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalUsed = new AtomicReference<>(BigDecimal.ZERO);

        // when
        for (int i = 0; i < totalOperations; i++) {
            final boolean isCharge = i % 2 == 0; // 짝수는 충전, 홀수는 사용
            final BigDecimal amount = new BigDecimal(500 + i * 100); // 금액을 다양하게

            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    if (isCharge) {
                        // 충전 실행
                        PointChargeCommand command = PointChargeCommand.builder()
                                .userID(userId)
                                .amount(amount)
                                .build();

                        facade.charge(command);
                        chargeSuccessCount.incrementAndGet();
                        totalCharged.getAndUpdate(current -> current.add(amount));
                    } else {
                        // 사용 실행 (직접 서비스 호출, 파사드를 통해 사용 메서드가 없으므로)
                        pointService.use(userId, amount);
                        useSuccessCount.incrementAndGet();
                        totalUsed.getAndUpdate(current -> current.add(amount));
                    }
                } catch (Exception e) {
                    log.error("작업 실패: {}, 금액: {}, 오류: {}",
                            isCharge ? "충전" : "사용", amount, e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // 모든 스레드 동시 시작
        latch.countDown();

        // 모든 스레드 완료 대기
        completionLatch.await(10, TimeUnit.SECONDS);

        // then
        log.info("성공한 충전 수: {}, 성공한 사용 수: {}, 실패 수: {}",
                chargeSuccessCount.get(), useSuccessCount.get(), failureCount.get());

        // 예상 포인트 계산
        BigDecimal expectedPoint = initialPoint
                .add(totalCharged.get())
                .subtract(totalUsed.get());

        // 최종 포인트 확인
        BigDecimal finalPoint = facade.getPoint(userId);
        log.info("초기 포인트: {}, 충전 총액: {}, 사용 총액: {}, 예상 포인트: {}, 실제 포인트: {}",
                initialPoint, totalCharged.get(), totalUsed.get(), expectedPoint, finalPoint);

        // 최종 포인트가 예상과 일치해야 함
        assertThat(finalPoint).isEqualTo(expectedPoint);

        // 충전 이력 확인
        List<DomainPointHistory> histories = pointHistoryRepository.findByUserId(userId);
        long chargeHistories = histories.stream()
                .filter(h -> h.getType() == PointTransactionType.CHARGE)
                .count();
        long useHistories = histories.stream()
                .filter(h -> h.getType() == PointTransactionType.USE)
                .count();

        assertThat(chargeHistories).isEqualTo(chargeSuccessCount.get());
        // 사용 이력은 별도로 만들지 않았으므로 확인하지 않음
    }

    @Test
    @DisplayName("음수 금액 충전 동시성 테스트")
    void 음수_금액_충전_동시성_테스트() throws InterruptedException {
        // given
        Long userId = 1L; // 일반 사용자
        BigDecimal initialPoint = facade.getPoint(userId);

        int concurrentOperations = 5;
        CountDownLatch completionLatch = new CountDownLatch(concurrentOperations);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < concurrentOperations; i++) {
            final BigDecimal amount = new BigDecimal(-1000); // 음수 금액

            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    PointChargeCommand command = PointChargeCommand.builder()
                            .userID(userId)
                            .amount(amount)
                            .build();

                    facade.charge(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("충전 실패: {}", e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // 모든 스레드 동시 시작
        latch.countDown();

        // 모든 스레드 완료 대기
        completionLatch.await(10, TimeUnit.SECONDS);

        // then
        log.info("성공한 충전 수: {}, 실패한 충전 수: {}", successCount.get(), failureCount.get());

        // 모든 충전이 실패해야 함 (음수 금액)
        assertThat(successCount.get()).isZero();
        assertThat(failureCount.get()).isEqualTo(concurrentOperations);

        // 포인트가 변경되지 않아야 함
        BigDecimal finalPoint = facade.getPoint(userId);
        assertThat(finalPoint).isEqualTo(initialPoint);

        // 충전 이력이 생성되지 않아야 함
        List<DomainPointHistory> histories = pointHistoryRepository.findByUserId(userId);
        long newChargeHistories = histories.stream()
                .filter(h -> h.getType() == PointTransactionType.CHARGE)
                .filter(h -> h.getCreatedDateTime().isAfter(LocalDateTime.now().minusMinutes(1)))
                .count();
        assertThat(newChargeHistories).isZero();
    }
}