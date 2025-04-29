package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.order.model.CreateOrderProductHistory;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.point.model.CreatePoint;
import kr.hhplus.be.server.domain.point.model.DomainPointHistory;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.model.CreateProduct;
import kr.hhplus.be.server.domain.product.model.CreateProductStock;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.repository.ProductRankRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.repository.ProductStockRepository;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.CreateUserCoupon;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.support.DatabaseCleanup;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("주문 파사드 통합 테스트")
class OrderFacadeIntegrationTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(OrderFacadeIntegrationTest.class);

    @Autowired
    private OrderFacade facade;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockRepository productStockRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private ProductRankRepository productRankRepository;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        CreateUser createUser = CreateUser.builder()
                .name("테스트유저")
                .id("testuser")
                .build();
        User user = userRepository.create(createUser);
        log.info(user.toString());

        // 충분한 포인트를 가진 사용자 생성
        pointRepository.create(user.getId());

        // 포인트가 부족한 사용자 생성
        createUser = CreateUser.builder()
                .name("부족한유저")
                .id("pooruser")
                .build();
        User poorUser = userRepository.create(createUser);

        pointRepository.create(user.getId());

        // 상품 생성
        CreateProduct createProduct1 = CreateProduct.builder()
                .name("상품1")
                .category(ProductCategory.ETC)
                .price(new BigDecimal(50_000))
                .productNumber("NUMBER01")
                .build();
        DomainProduct product1 = productRepository.create(createProduct1);

        CreateProduct createProduct2 = CreateProduct.builder()
                .name("상품2")
                .category(ProductCategory.ETC)
                .price(new BigDecimal(100_000))
                .productNumber("NUMBER02")
                .build();
        DomainProduct product2 = productRepository.create(createProduct2);

        CreateProduct createProduct3 = CreateProduct.builder()
                .name("상품3")
                .category(ProductCategory.ETC)
                .price(new BigDecimal(150_000))
                .productNumber("NUMBER03")
                .build();
        DomainProduct product3 = productRepository.create(createProduct3);

        // 상품 재고 생성
        CreateProductStock createStock1 = CreateProductStock.builder()
                .productId(product1.getId())
                .quantity(100)
                .build();
        productStockRepository.create(createStock1);

        CreateProductStock createStock2 = CreateProductStock.builder()
                .productId(product2.getId())
                .quantity(100)
                .build();
        productStockRepository.create(createStock2);

        CreateProductStock createStock3 = CreateProductStock.builder()
                .productId(product3.getId())
                .quantity(100)
                .build();
        productStockRepository.create(createStock3);

        // 사용자 쿠폰 생성
        CreateCoupon createCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();
        DomainCoupon coupon = couponRepository.create(createCoupon);

        userCouponRepository.create(new CreateUserCoupon(user.getId(), coupon.getId()));

        // 주문 이력 데이터 생성 (랭킹 테스트용)
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 3; i++) {
            for (int j = 0; j < i * 2; j++) {
                CreateOrderProductHistory history = new CreateOrderProductHistory(j + 1L, Long.valueOf(i), i + j);
                historyRepository.create(history);
            }
        }
    }

    @Test
    @DisplayName("정상 주문 통합 테스트")
    void 정상_주문_통합테스트() {
        // given
        Long userId = 1L;  // 충분한 포인트를 가진 사용자

        OrderCommand.OrderItem item1 = OrderCommand.OrderItem.builder()
                .productId(1L)
                .quantity(2)
                .build();

        OrderCommand.OrderItem item2 = OrderCommand.OrderItem.builder()
                .productId(2L)
                .quantity(1)
                .build();

        OrderCommand command = OrderCommand.builder()
                .userId(userId)
                .items(List.of(item1, item2))
                .build();

        // 주문 전 포인트 조회
        BigDecimal beforePoint = pointRepository.findByUserId(userId).get().getPoint();

        // 주문 전 상품 재고 조회
        int beforeStock1 = productStockRepository.findByProductId(1L).get().getQuantity();
        int beforeStock2 = productStockRepository.findByProductId(2L).get().getQuantity();

        // when
        facade.order(command);

        // then
        // 1. 주문이 생성되었는지 확인
        List<DomainOrder> orders = orderRepository.findByUserId(userId);
        assertThat(orders).isNotEmpty();
        DomainOrder createdOrder = orders.get(orders.size() - 1);

        // 2. 주문 상품이 정확히 포함되었는지 확인
        assertThat(createdOrder.getItems()).hasSize(2);

        // 3. 포인트가 차감되었는지 확인
        BigDecimal afterPoint = pointRepository.findByUserId(userId).get().getPoint();
        assertThat(beforePoint.subtract(createdOrder.getTotalPrice())).isEqualTo(afterPoint);

        // 4. 포인트 사용 이력이 기록되었는지 확인
        List<DomainPointHistory> pointHistories = pointHistoryRepository.findByUserId(userId);
        assertThat(pointHistories).isNotEmpty();

        // 5. 상품 재고가 감소했는지 확인
        int afterStock1 = productStockRepository.findByProductId(1L).get().getQuantity();
        int afterStock2 = productStockRepository.findByProductId(2L).get().getQuantity();
        assertThat(beforeStock1 - 2).isEqualTo(afterStock1);  // 2개 주문했으므로 2개 감소
        assertThat(beforeStock2 - 1).isEqualTo(afterStock2);  // 1개 주문했으므로 1개 감소
    }

    @Test
    @DisplayName("포인트 부족 주문 실패 통합 테스트")
    void 포인트_부족_주문_실패_통합테스트() {
        // given
        Long userId = 2L;  // 포인트가 부족한 사용자

        OrderCommand.OrderItem item1 = OrderCommand.OrderItem.builder()
                .productId(2L)  // 100,000원짜리 상품
                .quantity(1)
                .build();

        OrderCommand.OrderItem item2 = OrderCommand.OrderItem.builder()
                .productId(3L)  // 150,000원짜리 상품
                .quantity(1)
                .build();

        OrderCommand command = OrderCommand.builder()
                .userId(userId)
                .items(List.of(item1, item2))
                .build();

        // 주문 전 상품 재고 확인
        int beforeStock2 = productStockRepository.findByProductId(2L).get().getQuantity();
        int beforeStock3 = productStockRepository.findByProductId(3L).get().getQuantity();

        // when & then
        assertThatThrownBy(() -> facade.order(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessageContaining("잔액 부족");

        // 주문이 생성되었는지 확인 (롤백되어야 함)
        List<DomainOrder> orders = orderRepository.findByUserId(userId);
        assertThat(orders).isEmpty();

        // 재고가 차감되지 않았는지 확인 (롤백되어야 함)
        int afterStock2 = productStockRepository.findByProductId(2L).get().getQuantity();
        int afterStock3 = productStockRepository.findByProductId(3L).get().getQuantity();
        assertThat(beforeStock2).isEqualTo(afterStock2);
        assertThat(beforeStock3).isEqualTo(afterStock3);
    }

    @Test
    @DisplayName("쿠폰 적용 주문 통합 테스트")
    void 쿠폰_적용_주문_통합테스트() {
        // given
        Long userId = 1L;
        Long couponId = 1L;

        OrderCommand.OrderItem item = OrderCommand.OrderItem.builder()
                .productId(1L)
                .quantity(1)
                .couponId(couponId)  // 쿠폰 적용
                .build();

        OrderCommand command = OrderCommand.builder()
                .userId(userId)
                .items(List.of(item))
                .build();

        // 주문 전 포인트 확인
        BigDecimal beforePoint = pointRepository.findByUserId(userId).get().getPoint();

        // 쿠폰 사용 여부 확인
        boolean couponUsedBefore = userCouponRepository.findByUserIdAndCouponId(userId, couponId).get().getIsUsed();
        assertThat(couponUsedBefore).isFalse();

        // when
        facade.order(command);

        // then
        // 주문이 생성되었는지 확인
        List<DomainOrder> orders = orderRepository.findByUserId(userId);
        assertThat(orders).isNotEmpty();
        DomainOrder createdOrder = orders.get(orders.size() - 1);

        // 쿠폰이 사용 처리되었는지 확인
        boolean couponUsedAfter = userCouponRepository.findByUserIdAndCouponId(userId, couponId).get().getIsUsed();
        assertThat(couponUsedAfter).isTrue();

        // 할인이 적용되었는지 확인 (상품가격 50,000 - 쿠폰할인 5,000 = 45,000)
        assertThat(createdOrder.getTotalPrice()).isEqualTo(new BigDecimal(45_000));

        // 포인트가 정확히 차감되었는지 확인
        BigDecimal afterPoint = pointRepository.findByUserId(userId).get().getPoint();
        assertThat(beforePoint.subtract(new BigDecimal(45_000))).isEqualTo(afterPoint);
    }

    @Test
    @DisplayName("0원 주문 통합 테스트")
    void 영원_주문_통합테스트() {
        // given
        Long userId = 1L;

        // 0원 상품 생성
        CreateProduct createFreeProduct = CreateProduct.builder()
                .name("무료상품")
                .price(BigDecimal.ZERO)
                .build();
        DomainProduct freeProduct = productRepository.create(createFreeProduct);

        CreateProductStock createFreeStock = CreateProductStock.builder()
                .productId(freeProduct.getId())
                .quantity(100)
                .build();
        productStockRepository.create(createFreeStock);

        OrderCommand.OrderItem item = OrderCommand.OrderItem.builder()
                .productId(freeProduct.getId())
                .quantity(1)
                .build();

        OrderCommand command = OrderCommand.builder()
                .userId(userId)
                .items(List.of(item))
                .build();

        // 주문 전 포인트 확인
        BigDecimal beforePoint = pointRepository.findByUserId(userId).get().getPoint();

        // when
        facade.order(command);

        // then
        // 주문이 생성되었는지 확인
        List<DomainOrder> orders = orderRepository.findByUserId(userId);
        assertThat(orders).isNotEmpty();

        // 0원 주문이므로 포인트가 차감되지 않아야 함
        BigDecimal afterPoint = pointRepository.findByUserId(userId).get().getPoint();
        assertThat(afterPoint).isEqualTo(beforePoint);

        // 포인트 사용 이력이 기록되지 않아야 함
        List<DomainPointHistory> pointHistories = pointHistoryRepository.findByUserId(userId);
        assertThat(pointHistories).isEmpty();
    }

    @Test
    @DisplayName("여러 상품 주문 통합 테스트")
    void 여러_상품_주문_통합테스트() {
        // given
        Long userId = 1L;

        OrderCommand.OrderItem item1 = OrderCommand.OrderItem.builder()
                .productId(1L)
                .quantity(1)
                .build();

        OrderCommand.OrderItem item2 = OrderCommand.OrderItem.builder()
                .productId(2L)
                .quantity(2)
                .build();

        OrderCommand.OrderItem item3 = OrderCommand.OrderItem.builder()
                .productId(3L)
                .quantity(1)
                .build();

        OrderCommand command = OrderCommand.builder()
                .userId(userId)
                .items(List.of(item1, item2, item3))
                .build();

        // 주문 전 포인트 확인
        BigDecimal beforePoint = pointRepository.findByUserId(userId).get().getPoint();

        // 예상 주문 금액: 상품1(50,000) + 상품2(100,000*2) + 상품3(150,000) = 400,000원
        BigDecimal expectedTotalPrice = new BigDecimal(400_000);

        // when
        facade.order(command);

        // then
        // 주문이 생성되었는지 확인
        List<DomainOrder> orders = orderRepository.findByUserId(userId);
        assertThat(orders).isNotEmpty();
        DomainOrder createdOrder = orders.get(orders.size() - 1);

        // 주문 금액이 정확한지 확인
        assertThat(createdOrder.getTotalPrice()).isEqualTo(expectedTotalPrice);

        // 주문 아이템이 3개인지 확인
        assertThat(createdOrder.getItems()).hasSize(3);

        // 포인트가 정확히 차감되었는지 확인
        BigDecimal afterPoint = pointRepository.findByUserId(userId).get().getPoint();
        assertThat(beforePoint.subtract(expectedTotalPrice)).isEqualTo(afterPoint);
    }

    @Test
    @DisplayName("상품 랭킹 업데이트 통합 테스트")
    void 상품_랭킹_업데이트_통합테스트() {
        // given
        // setUp에서 이미 주문 이력 데이터를 생성했음

        // when
        facade.updateRank();

        // then
        // 랭킹이 생성되었는지 확인
        List<DomainProductRank> ranks = productRankRepository.findAll();
        assertThat(ranks).isNotEmpty();
        assertThat(ranks).hasSize(3);  // 3개의 상품에 대한 랭킹이 있어야 함

        // 랭킹 순서가 정확한지 확인 - 주문량에 따라 정렬되어야 함
        List<Long> productIds = ranks.stream()
                .sorted(Comparator.comparing(DomainProductRank::getRank))
                .map(DomainProductRank::getProductId)
                .collect(Collectors.toList());

        // 주문량이 가장 많은 순서대로 정렬되어야 함 (3, 2, 1)
        assertThat(productIds).containsExactly(3L, 2L, 1L);
    }

    @Test
    @DisplayName("주문 번호 생성 테스트")
    void 주문번호_생성_테스트() {
        // when
        String orderNumber = facade.createOrderNumber();

        // then
        assertThat(orderNumber).isNotNull();
        assertThat(orderNumber).isNotEmpty();

        LocalDateTime now = LocalDateTime.now();
        String expectedPrefix = String.format("{0}{1}{2}", now.getYear(), now.getMonth(), now.getDayOfMonth());

        // 주문번호 형식 확인
        assertThat(orderNumber).startsWith(expectedPrefix);
        assertThat(orderNumber.length()).isGreaterThanOrEqualTo(expectedPrefix.length() + 8);  // 8자리 난수 포함
    }

    @Test
    @DisplayName("존재하지 않는 사용자 주문 실패 테스트")
    void 존재하지_않는_사용자_주문_실패_테스트() {
        // given
        Long nonExistingUserId = 999L;

        OrderCommand.OrderItem item = OrderCommand.OrderItem.builder()
                .productId(1L)
                .quantity(1)
                .build();

        OrderCommand command = OrderCommand.builder()
                .userId(nonExistingUserId)
                .items(List.of(item))
                .build();

        // when & then
        assertThatThrownBy(() -> facade.order(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessageContaining("없는 사용자");
    }

    @Test
    @DisplayName("존재하지 않는 상품 주문 실패 테스트")
    void 존재하지_않는_상품_주문_실패_테스트() {
        // given
        Long userId = 1L;
        Long nonExistingProductId = 999L;

        OrderCommand.OrderItem item = OrderCommand.OrderItem.builder()
                .productId(nonExistingProductId)
                .quantity(1)
                .build();

        OrderCommand command = OrderCommand.builder()
                .userId(userId)
                .items(List.of(item))
                .build();

        // when & then
        assertThatThrownBy(() -> facade.order(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessageContaining("상품이 없습니다");
    }

}