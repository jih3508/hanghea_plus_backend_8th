package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.order.model.CreateOrderProductHistory;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.model.*;
import kr.hhplus.be.server.domain.product.repository.ProductRankRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.repository.ProductStockRepository;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.CreateUserCoupon;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("주문 파사드 동시성 테스트")
class OrderFacadeConcurrencyTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(OrderFacadeConcurrencyTest.class);

    @Autowired
    private OrderFacade facade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderProductHistoryRepository historyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private ProductStockRepository productStockRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private ProductRankRepository productRankRepository;


    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService executorService;
    private CountDownLatch latch;

    private User user1;

    private User user2;

    private DomainProductStock product1;

    private DomainProductStock product2;

    @BeforeEach
    void setUp() {
        // 동시 실행을 위한 스레드풀 및 래치 설정
        executorService = Executors.newFixedThreadPool(10);
        latch = new CountDownLatch(1);

        // 테스트용 사용자 생성
        transactionTemplate.execute(status -> {
            CreateUser createUser = CreateUser.builder()
                    .name("동시성테스트유저1")
                    .id("concurrency user1")
                    .build();
            user1 = userRepository.create(createUser);

            // 포인트 설정Decimal(100_000))
            pointRepository.create(user1.getId(), BigDecimal.valueOf(1_000_000_000));

            createUser = CreateUser.builder()
                    .name("동시성테스트유저2")
                    .id("concurrency user2")
                    .build();
            user2 = userRepository.create(createUser);
            pointRepository.create(user2.getId(), BigDecimal.valueOf(1_000_000_000));

            product1 = createProduct("맥북", BigDecimal.valueOf(1_000_000), ProductCategory.ELECTRONIC_DEVICES ,10);
            product2 = createProduct("순대", BigDecimal.valueOf(5_000), ProductCategory.FOOD, 20);

            return null;
        });
    }

    DomainProductStock createProduct(String name, BigDecimal price, ProductCategory category, Integer quantity) {
        CreateProduct createProduct = CreateProduct.builder()
                .name(name)
                .productNumber(UUID.randomUUID().toString())
                .price(price)
                .category(category)
                .build();
        DomainProduct limitedProduct = productRepository.create(createProduct);
        // 한정 수량 재고 설정
        CreateProductStock createStock = CreateProductStock.builder()
                .productId(limitedProduct.getId())
                .quantity(quantity) // 5개만 있는 한정 상품
                .build();
        return productStockRepository.create(createStock);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    @DisplayName("재고 부족 동시성 실패 테스트")
    void 재고_부족_동시성_실패_테스트() throws InterruptedException {
        // given
        Long userId = 1L; // 동시성 테스트 사용자
        Long productId = 1L; // 한정 수량 상품

        // 10명의 사용자가 동시에 5개 재고의 상품을 1개씩 주문
        int concurrentUsers = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(concurrentUsers);

        // when
        // 10개의 스레드에서 동시에 주문 시도
        for (int i = 0; i < concurrentUsers; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    OrderCommand.OrderItem item = OrderCommand.OrderItem.builder()
                            .productId(productId)
                            .quantity(1)
                            .build();

                    OrderCommand command = OrderCommand.builder()
                            .userId(userId)
                            .items(List.of(item))
                            .build();

                    // 주문 실행
                    facade.order(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("주문 실패: {}", e.getMessage());
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
        log.info("성공한 주문 수: {}, 실패한 주문 수: {}", successCount.get(), failureCount.get());

        // 성공한 주문은 5개 이하여야 함 (재고가 5개)
        assertThat(successCount.get()).isLessThanOrEqualTo(5);

        // 실패한 주문은 5개 이상이어야 함
        assertThat(failureCount.get()).isGreaterThanOrEqualTo(5);

        // 총 주문 시도는 10개
        assertThat(successCount.get() + failureCount.get()).isEqualTo(10);

        // 데이터베이스에서 실제 재고 확인
        DomainProductStock stock = productStockRepository.findByProductId(productId).get();
        assertThat(stock.getQuantity()).isEqualTo(0); // 모든 재고가 소진되어야 함
    }

    @Test
    @DisplayName("포인트 차감 동시성 실패 테스트")
    void 포인트_차감_동시성_실패_테스트() throws InterruptedException {
        // given
        Long userId = 1L; // 동시성 테스트 사용자
        Long productId = 1L; // 한정 수량 상품

        // 포인트를 정확히 설정 (4개 주문만 가능한 금액으로)
        transactionTemplate.execute(status -> {
            // 기존 포인트 삭제 후 재설정
            pointRepository.delete(userId);
            pointRepository.create(1L);
            return null;
        });

        // 8명의 사용자가 동시에 주문 (포인트는 4개 주문만 가능)
        int concurrentUsers = 8;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(concurrentUsers);

        // when
        for (int i = 0; i < concurrentUsers; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    OrderCommand.OrderItem item = OrderCommand.OrderItem.builder()
                            .productId(productId)
                            .quantity(1)
                            .build();

                    OrderCommand command = OrderCommand.builder()
                            .userId(userId)
                            .items(List.of(item))
                            .build();

                    // 주문 실행
                    facade.order(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("주문 실패: {}", e.getMessage());
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
        log.info("성공한 주문 수: {}, 실패한 주문 수: {}", successCount.get(), failureCount.get());

        // 성공한 주문은 4개 이하여야 함 (포인트로 4개까지만 구매 가능)
        assertThat(successCount.get()).isLessThanOrEqualTo(4);

        // 실패한 주문은 4개 이상이어야 함
        assertThat(failureCount.get()).isGreaterThanOrEqualTo(4);

        // 총 주문 시도는 8개
        assertThat(successCount.get() + failureCount.get()).isEqualTo(8);

        // 데이터베이스에서 실제 포인트 확인
        DomainPoint point = pointRepository.findByUserId(userId).get();
        assertThat(point.getPoint()).isLessThanOrEqualTo(BigDecimal.ZERO); // 포인트가 소진되어야 함
    }

    @Test
    @DisplayName("쿠폰 중복 사용 동시성 실패 테스트")
    void 쿠폰_중복_사용_동시성_실패_테스트() throws InterruptedException {
        // given
        Long userId = 1L;
        Long couponId = null;

        // 트랜잭션 내에서 쿠폰 생성
        couponId = transactionTemplate.execute(status -> {
            // 쿠폰 생성
            CreateCoupon createCoupon = CreateCoupon.builder()
                    .couponNumber(UUID.randomUUID().toString())
                    .type(CouponType.FLAT)
                    .discountPrice(new BigDecimal(5_000))
                    .quantity(1)
                    .startDateTime(LocalDateTime.now().minusDays(1))
                    .endDateTime(LocalDateTime.now().plusDays(30))
                    .build();
            DomainCoupon coupon = couponRepository.create(createCoupon);

            // 사용자에게 쿠폰 발급
            userCouponRepository.create(new CreateUserCoupon(userId, coupon.getId()));

            return coupon.getId();
        });

        final Long finalCouponId = couponId;

        // 5명의 사용자가 동시에 같은 쿠폰을 사용하여 주문
        int concurrentUsers = 5;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(concurrentUsers);

        // when
        for (int i = 0; i < concurrentUsers; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    OrderCommand.OrderItem item = OrderCommand.OrderItem.builder()
                            .productId(1L)
                            .quantity(1)
                            .couponId(finalCouponId) // 모두 같은 쿠폰 사용 시도
                            .build();

                    OrderCommand command = OrderCommand.builder()
                            .userId(userId)
                            .items(List.of(item))
                            .build();

                    // 주문 실행
                    facade.order(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("주문 실패: {}", e.getMessage());
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
        log.info("성공한 주문 수: {}, 실패한 주문 수: {}", successCount.get(), failureCount.get());

        // 성공한 주문은 1개여야 함 (쿠폰은 한 번만 사용 가능)
        assertThat(successCount.get()).isEqualTo(1);

        // 실패한 주문은 4개여야 함
        assertThat(failureCount.get()).isEqualTo(4);

        // 쿠폰이 사용됨 상태인지 확인
        DomainUserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, finalCouponId).get();
        assertThat(userCoupon.getIsUsed()).isTrue();
    }

    @Test
    @DisplayName("랭킹 업데이트 동시성 테스트")
    void 랭킹_업데이트_동시성_테스트() throws InterruptedException {
        // given
        // 다수의 주문 이력 데이터 생성
        transactionTemplate.execute(status -> {
            for (int i = 1; i <= 3; i++) {
                for (int j = 0; j < 100; j++) {
                    CreateOrderProductHistory history = new CreateOrderProductHistory(j + 1L, Long.valueOf(i), i + j % 5);
                    historyRepository.create(history);
                }
            }
            return null;
        });

        // 5개의 스레드에서 동시에 랭킹 업데이트 시도
        int concurrentThreads = 5;
        CountDownLatch completionLatch = new CountDownLatch(concurrentThreads);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < concurrentThreads; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    // 랭킹 업데이트 실행
                    facade.updateRank();
                } catch (Exception e) {
                    log.error("랭킹 업데이트 실패: {}", e.getMessage());
                    exceptionCount.incrementAndGet();
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
        // 동시 업데이트로 인한 예외가 발생할 수 있음 (데이터베이스 락 충돌 등)
        log.info("랭킹 업데이트 실패 수: {}", exceptionCount.get());

        // 랭킹 테이블에 데이터가 존재해야 함
        List<DomainProductRank> ranks = productRankRepository.findAll();
        assertThat(ranks).isNotEmpty();

        // 상품 ID에 따라 랭킹이 정렬되어 있어야 함
        ranks.sort(Comparator.comparing(DomainProductRank::getRank));
        for (int i = 0; i < ranks.size() - 1; i++) {
            assertThat(ranks.get(i).getTotalQuantity())
                    .isGreaterThanOrEqualTo(ranks.get(i + 1).getTotalQuantity());
        }
    }

    @Test
    @DisplayName("주문 생성과 포인트 사용의 원자성 테스트")
    void 주문_생성과_포인트_사용_원자성_테스트() throws InterruptedException {
        // given
        Long userId = 1L;

        // 주문 처리 중 예외 발생을 시뮬레이션하기 위한 테스트 상품 생성
        Long errorProductId = transactionTemplate.execute(status -> {
            CreateProduct createErrorProduct = CreateProduct.builder()
                    .name("에러발생상품")
                    .price(new BigDecimal(999_999)) // 높은 가격으로 설정
                    .build();
            DomainProduct errorProduct = productRepository.create(createErrorProduct);

            // 재고 설정
            CreateProductStock createErrorStock = CreateProductStock.builder()
                    .productId(errorProduct.getId())
                    .quantity(10)
                    .build();
            productStockRepository.create(createErrorStock);

            return errorProduct.getId();
        });

        // 원래 포인트 조회
        BigDecimal originalPoint = pointRepository.findByUserId(userId).get().getPoint();

        // 동시 주문 시도 수
        int concurrentOrders = 3;
        CountDownLatch completionLatch = new CountDownLatch(concurrentOrders);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < concurrentOrders; i++) {
            final int orderIndex = i;
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    OrderCommand.OrderItem item = OrderCommand.OrderItem.builder()
                            .productId(errorProductId)
                            .quantity(1)
                            .build();

                    OrderCommand command = OrderCommand.builder()
                            .userId(userId)
                            .items(List.of(item))
                            .build();

                    // 마지막 주문에서는 예외를 발생시키기 위해 포인트 서비스를 모킹
                    if (orderIndex == concurrentOrders - 1) {
                        // 이 테스트는 실제 모킹이 불가능하므로, 포인트가 부족한 상황을 만들어 예외 발생
                        // 포인트를 0으로 설정
                        transactionTemplate.execute(status -> {
                            pointRepository.delete(userId);
                            pointRepository.create(userId);
                            return null;
                        });
                    }

                    // 주문 실행
                    facade.order(command);
                } catch (Exception e) {
                    log.error("주문 처리 중 예외 발생: {}", e.getMessage());
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
        // 실패한 주문이 있어야 함
        assertThat(failureCount.get()).isGreaterThan(0);

        // 주문과 포인트 사용의 원자성 확인
        // 성공한 주문 수와 포인트 차감 횟수가 일치해야 함
        int successfulOrders = concurrentOrders - failureCount.get();

        // 주문 수 확인
        List<DomainOrder> orders = orderRepository.findByUserId(userId);
        int ordersForErrorProduct = 0;
        for (DomainOrder order : orders) {
            for (DomainOrderItem item : order.getItems()) {
                if (item.getProductId().equals(errorProductId)) {
                    ordersForErrorProduct++;
                    break;
                }
            }
        }

        // 실제 생성된 주문 수는 성공한 주문 수와 일치해야 함
        assertThat(ordersForErrorProduct).isEqualTo(successfulOrders);
    }

    @Test
    @DisplayName("재고 분산락 적용 테스트 -> 재고 부족")
    void 재고_분산락_재고_부족() throws InterruptedException {
        //given
        OrderCommand orderCommand1 = OrderCommand.builder()
                .userId(user1.getId())
                .items(List.of(OrderCommand.OrderItem.builder()
                                .productId(product1.getProductId())
                                .quantity(5)
                                .build(),
                        OrderCommand.OrderItem.builder()
                                .productId(product2.getProductId())
                                .quantity(10)
                                .build()
                )).build();

        OrderCommand orderCommand2 = OrderCommand.builder()
                .userId(user2.getId())
                .items(List.of(OrderCommand.OrderItem.builder()
                                .productId(product1.getProductId())
                                .quantity(5)
                                .build(),
                        OrderCommand.OrderItem.builder()
                                .productId(product2.getProductId())
                                .quantity(20)
                                .build()
                )).build();

        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        executorService.submit(() -> {
            try {
                facade.order(orderCommand1);
            }catch (Exception e){
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                facade.order(orderCommand2);
            }catch (Exception e){
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }

        });

        latch.await();
        executorService.shutdown();

        //then
        DomainProductStock stock1 =  productStockRepository.findByProductId(product1.getProductId()).get();
        DomainProductStock stock2 =  productStockRepository.findByProductId(product2.getProductId()).get();
        log.info(stock1.toString());
        log.info(stock2.toString());

        assertThat(failureCount.get()).isGreaterThan(1);


    }

    @Test
    @DisplayName("재고 분산락 적용 테스트")
    void 재고_분산락() throws InterruptedException {
        //given
        OrderCommand orderCommand1 = OrderCommand.builder()
                .userId(user1.getId())
                .items(List.of(OrderCommand.OrderItem.builder()
                        .productId(product1.getProductId())
                        .quantity(5)
                        .build(),
                        OrderCommand.OrderItem.builder()
                                .productId(product2.getProductId())
                                .quantity(10)
                                .build()
                        )).build();

        OrderCommand orderCommand2 = OrderCommand.builder()
                .userId(user2.getId())
                .items(List.of(OrderCommand.OrderItem.builder()
                                .productId(product1.getProductId())
                                .quantity(5)
                                .build(),
                        OrderCommand.OrderItem.builder()
                                .productId(product2.getProductId())
                                .quantity(10)
                                .build()
                )).build();

        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // when
        executorService.submit(() -> {
            try {
                facade.order(orderCommand1);
            }finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                facade.order(orderCommand2);
            }finally {
                latch.countDown();
            }

        });

        latch.await();
        executorService.shutdown();

        //then
        DomainProductStock stock1 =  productStockRepository.findByProductId(product1.getProductId()).get();
        DomainProductStock stock2 =  productStockRepository.findByProductId(product2.getProductId()).get();

        log.info(stock1.toString());
        log.info(stock2.toString());
        assertThat(stock1.getQuantity()).isEqualTo(0);
        assertThat(stock2.getQuantity()).isEqualTo(0);


    }


}