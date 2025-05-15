package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.util.RedisKeysPrefix;
import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.CreateUserCoupon;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("쿠폰 파사드 동시성 테스트")
class CouponFacadeConcurrencyTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(CouponFacadeConcurrencyTest.class);

    @Autowired
    private CouponFacade facade;

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService executorService;
    private CountDownLatch latch;

    private User user1;
    private List<User> users;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    private final String redisKey = RedisKeysPrefix.COUPON_KEY_PREFIX;

    @BeforeEach
    void setUp() {
        // 동시 실행을 위한 스레드풀 및 래치 설정
        users = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(10);
        latch = new CountDownLatch(1);

        user1 = createUser("test1", "홍길동");
        users.add(user1);
        users.add(createUser("test2", "이순신"));
        users.add(createUser("test3", "강감찬"));


    }

    User createUser(String id, String name) {
        return userRepository.create(
                CreateUser.builder()
                        .id(id)
                        .name(name)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    @DisplayName("한정 수량 쿠폰 동시 발급 테스트")
    void 한정_수량_쿠폰_동시_발급_테스트() throws InterruptedException {
        // given
        // 테스트 사용자 생성
        Long userId = transactionTemplate.execute(status -> {
            CreateUser createUser = CreateUser.builder()
                    .name("쿠폰테스트유저")
                    .id("couponuser")
                    .build();
            User user = userRepository.create(createUser);
            return user.getId();
        });

        // 10개 한정 쿠폰 생성
        Long limitedCouponId = transactionTemplate.execute(status -> {
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

        redisTemplate.opsForValue().set(redisKey + limitedCouponId, 10L);

        // 20명의 사용자가 동시에 10개의 한정 쿠폰을 신청
        int concurrentRequests = 20;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);

        // when
        for (int i = 0; i < concurrentRequests; i++) {
            final int userIdx = i; // 각 요청을 구분하기 위한 인덱스
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    CouponIssueCommand command = CouponIssueCommand.builder()
                            .userId(userId)
                            .couponId(limitedCouponId)
                            .build();

                    facade.issue(command);
                    log.info("쿠폰 발급 성공: 사용자 요청 #{}", userIdx);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("쿠폰 발급 실패: 사용자 요청 #{}, 오류: {}", userIdx, e.getMessage());
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
        log.info("성공한 발급 수: {}, 실패한 발급 수: {}", successCount.get(), failureCount.get());

        // 성공한 발급은 10개 이하여야 함 (쿠폰이 10개)
        assertThat(successCount.get()).isLessThanOrEqualTo(10);

        // 실패한 발급은 10개 이상이어야 함
        assertThat(failureCount.get()).isGreaterThanOrEqualTo(10);

        // 총 발급 시도는 20개
        assertThat(successCount.get() + failureCount.get()).isEqualTo(20);

        // 레디스에서 실제 쿠폰 남은 수량 확인
        long count = redisTemplate.opsForValue().get(redisKey + limitedCouponId);
        assertThat(count).isLessThan(0); // 모든 쿠폰이 소진되어야 함

        // 사용자에게 발급된 쿠폰 수 확인
        List<DomainUserCoupon> userCoupons = userCouponService.getUserCoupons(userId);
        assertThat(userCoupons.size()).isEqualTo(successCount.get());
    }

    @Test
    @DisplayName("다중 사용자 쿠폰 동시 발급 테스트")
    void 다중_사용자_쿠폰_동시_발급_테스트() throws InterruptedException {
        // given
        // 10명의 테스트 사용자 생성
        List<Long> userIds = transactionTemplate.execute(status -> {
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                CreateUser createUser = CreateUser.builder()
                        .name("쿠폰테스트유저" + i)
                        .id("couponuser" + i)
                        .build();
                User user = userRepository.create(createUser);
                ids.add(user.getId());
            }
            return ids;
        });

        // 5개 한정 쿠폰 생성
        Long limitedCouponId = transactionTemplate.execute(status -> {
            CreateCoupon createCoupon = CreateCoupon.builder()
                    .couponNumber(UUID.randomUUID().toString())
                    .type(CouponType.FLAT)
                    .discountPrice(new BigDecimal(5_000))
                    .quantity(5) // 5개 한정 쿠폰
                    .startDateTime(LocalDateTime.now().minusDays(1))
                    .endDateTime(LocalDateTime.now().plusDays(30))
                    .build();
            DomainCoupon coupon = couponRepository.create(createCoupon);
            return coupon.getId();
        });

        redisTemplate.opsForValue().set(redisKey + limitedCouponId, 5L);

        // 10명의 사용자가 동시에 5개의 한정 쿠폰을 신청
        int concurrentUsers = userIds.size();
        Map<Long, Boolean> userSuccessMap = new ConcurrentHashMap<>();
        CountDownLatch completionLatch = new CountDownLatch(concurrentUsers);

        // when
        for (int i = 0; i < concurrentUsers; i++) {
            final Long userId = userIds.get(i);
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    CouponIssueCommand command = CouponIssueCommand.builder()
                            .userId(userId)
                            .couponId(limitedCouponId)
                            .build();

                    facade.issue(command);
                    log.info("쿠폰 발급 성공: 사용자 ID {}", userId);
                    userSuccessMap.put(userId, true);
                } catch (Exception e) {
                    log.error("쿠폰 발급 실패: 사용자 ID {}, 오류: {}", userId, e.getMessage());
                    userSuccessMap.put(userId, false);
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
        int successCount = (int) userSuccessMap.values().stream().filter(v -> v).count();
        int failureCount = (int) userSuccessMap.values().stream().filter(v -> !v).count();

        log.info("성공한 발급 수: {}, 실패한 발급 수: {}", successCount, failureCount);

        // 성공한 발급은 5개 이하여야 함 (쿠폰이 5개)
        assertThat(successCount).isLessThanOrEqualTo(5);

        // 실패한 발급은 5개 이상이어야 함
        assertThat(failureCount).isGreaterThanOrEqualTo(5);

        // 총 발급 시도는 10개
        assertThat(successCount + failureCount).isEqualTo(10);

        // 데이터베이스에서 실제 쿠폰 남은 수량 확인
        //DomainCoupon coupon = couponService.getCoupon(limitedCouponId);
        //assertThat(coupon.getQuantity()).isEqualTo(0); // 모든 쿠폰이 소진되어야 함

        // 레디스에서 실제 쿠폰 남은 수량 확인
        long count = redisTemplate.opsForValue().get(redisKey + limitedCouponId);
        assertThat(count).isLessThan(0); // 모든 쿠폰이 소진되어야 함

        // 각 사용자별 쿠폰 발급 확인
        for (Long userId : userIds) {
            List<DomainUserCoupon> userCoupons = userCouponService.getUserCoupons(userId);
            Boolean success = userSuccessMap.get(userId);

            if (Boolean.TRUE.equals(success)) {
                assertThat(userCoupons).isNotEmpty();
                assertThat(userCoupons.stream()
                        .anyMatch(uc -> uc.getCouponId().equals(limitedCouponId))).isTrue();
            }
        }
    }



    @Test
    @DisplayName("존재하지 않는 쿠폰 동시 발급 테스트")
    void 존재하지_않는_쿠폰_동시_발급_테스트() throws InterruptedException {
        // given
        // 테스트 사용자 생성
        Long userId = transactionTemplate.execute(status -> {
            CreateUser createUser = CreateUser.builder()
                    .name("오류테스트유저")
                    .id("erroruser")
                    .build();
            User user = userRepository.create(createUser);
            return user.getId();
        });

        // 존재하지 않는 쿠폰 ID
        Long nonExistingCouponId = 9999L;

        // 5개의 동시 발급 요청
        int concurrentRequests = 5;
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);

        // when
        for (int i = 0; i < concurrentRequests; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    CouponIssueCommand command = CouponIssueCommand.builder()
                            .userId(userId)
                            .couponId(nonExistingCouponId)
                            .build();

                    facade.issue(command);
                } catch (Exception e) {
                    log.error("예상된 발급 실패: {}", e.getMessage());
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
        // 모든 요청이 실패해야 함
        assertThat(failureCount.get()).isEqualTo(concurrentRequests);

        // 사용자에게 발급된 쿠폰이 없어야 함
        List<DomainUserCoupon> userCoupons = userCouponService.getUserCoupons(userId);
        assertThat(userCoupons).isEmpty();
    }

    @Test
    @DisplayName("발급된 쿠폰 동시 조회 테스트")
    void 발급된_쿠폰_동시_조회_테스트() throws InterruptedException {
        // given
        // 테스트 사용자 생성
        Long userId = transactionTemplate.execute(status -> {
            CreateUser createUser = CreateUser.builder()
                    .name("조회테스트유저")
                    .id("lookupuser")
                    .build();
            User user = userRepository.create(createUser);
            return user.getId();
        });

        // 여러 개의 쿠폰 생성 및 발급
        transactionTemplate.execute(status -> {
            for (int i = 0; i < 5; i++) {
                CreateCoupon createCoupon = CreateCoupon.builder()
                        .couponNumber(UUID.randomUUID().toString())
                        .type(i % 2 == 0 ? CouponType.FLAT : CouponType.RATE)
                        .discountPrice(i % 2 == 0 ? new BigDecimal(5_000 + i * 1000) : null)
                        .rate(i % 2 == 0 ? null : 10 + i * 5)
                        .quantity(10)
                        .startDateTime(LocalDateTime.now().minusDays(1))
                        .endDateTime(LocalDateTime.now().plusDays(30))
                        .build();
                DomainCoupon coupon = couponRepository.create(createCoupon);

                // 사용자에게 쿠폰 발급
                userCouponRepository.create(new CreateUserCoupon(userId, coupon.getId()));
            }
            return null;
        });

        // 동시에 여러 번의 조회 실행
        int concurrentLookups = 20;
        CountDownLatch completionLatch = new CountDownLatch(concurrentLookups);
        List<List<CouponMeCommand>> results = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < concurrentLookups; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    // 쿠폰 조회
                    List<CouponMeCommand> userCoupons = facade.getMeCoupons(userId);
                    results.add(userCoupons);
                } catch (Exception e) {
                    log.error("쿠폰 조회 실패: {}", e.getMessage());
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
        // 모든 조회 결과가 동일해야 함 (읽기 작업은 일관성이 유지되어야 함)
        assertThat(results).isNotEmpty();

        int expectedSize = results.get(0).size();
        for (List<CouponMeCommand> result : results) {
            assertThat(result).hasSize(expectedSize);
        }

        // 각 결과셋의 쿠폰 ID들이 모두 동일한지 확인
        Set<Long> expectedCouponIds = results.get(0).stream()
                .map(CouponMeCommand::getCouponId)
                .collect(Collectors.toSet());

        for (List<CouponMeCommand> result : results) {
            Set<Long> couponIds = result.stream()
                    .map(CouponMeCommand::getCouponId)
                    .collect(Collectors.toSet());

            assertThat(couponIds).containsExactlyInAnyOrderElementsOf(expectedCouponIds);
        }
    }

    @Test
    @DisplayName("분산락 적용 테스트 -> 쿠폰 10명한테 동시에 발급되면 결과는 10개가 나와야 한다.")
    void 분산락_적용_쿠폰_발급() throws InterruptedException {
        // given
        Integer quantity = 20;
        CreateCoupon createCoupon1 = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(BigDecimal.valueOf(10_000))
                .startDateTime(LocalDateTime.now().minusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(30))
                .quantity(quantity)
                .build();

        DomainCoupon coupon1 = couponRepository.create(createCoupon1);

        redisTemplate.opsForValue().set(redisKey + coupon1.getId(), quantity.longValue());
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    Long userId = users.get((int) (Math.random() % users.size())).getId();
                    facade.issue(CouponIssueCommand.of(userId, coupon1.getId()));
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        DomainCoupon result = couponRepository.findById(coupon1.getId()).get();

        // 레디스에서 실제 쿠폰 남은 수량 확인
        long count = redisTemplate.opsForValue().get(redisKey + coupon1.getId());
        assertThat(count).isEqualTo(quantity - numberOfThreads);

    }


    @Test
    @DisplayName("분산락 적용 테스트 -> 쿠폰 10명한테 동시에 발급되면 5명 초과가 나서 발급 안되도록 해야 한다.")
    void 분산락_적용_쿠폰_발급_초과() throws InterruptedException {
        // given
        Integer quantity = 5;
        CreateCoupon createCoupon1 = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(BigDecimal.valueOf(10_000))
                .startDateTime(LocalDateTime.now().minusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(30))
                .quantity(quantity)
                .build();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        DomainCoupon coupon1 = couponRepository.create(createCoupon1);

        redisTemplate.opsForValue().set(redisKey + coupon1.getId(), quantity.longValue());
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    Long userId = users.get((int) (Math.random() % users.size())).getId();
                    facade.issue(CouponIssueCommand.of(userId, coupon1.getId()));
                    successCount.incrementAndGet();
                }catch (Exception e){
                    failCount.incrementAndGet();
                }
                finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        //DomainCoupon result = couponRepository.findById(coupon1.getId()).get();
        long count = redisTemplate.opsForValue().get(redisKey + coupon1.getId());
        assertThat(count).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failCount.get()).isEqualTo(5);

    }
}