package kr.hhplus.be.server.application.order;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.LockStrategy;
import kr.hhplus.be.server.common.lock.LockType;
import kr.hhplus.be.server.domain.external.ExternalTransmissionService;
import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.point.service.PointHistoryService;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.service.ProductRankRedisService;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacadeRedis {

    private final OrderService service;
    private final UserService userService;
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;
    private final ProductService productService;
    private final ProductStockService productStockService;
    private final UserCouponService userCouponService;
    private final ExternalTransmissionService externalTransmissionService;
    private final ProductRankRedisService productRankRedisService;

    @DistributedLock(key = "#command.getProductIdsAsString()", type = LockType.STOCK, strategy = LockStrategy.PUB_SUB_LOCK)
    @Transactional
    public void order(OrderCommand command) {
        DomainUser user = userService.findById(command.getUserId());
        BigDecimal totalPrice = BigDecimal.ZERO;
        CreateOrder createOrder = new CreateOrder(command.getUserId(), createOrderNumber());

        for (OrderCommand.OrderItem item : command.getItems()) {
            DomainProduct product = productService.getProduct(item.getProductId());
            productStockService.delivering(product.getId(), item.getQuantity());

            DomainUserCoupon userCoupon = null;
            if (item.getCouponId() != null) {
                userCoupon = userCouponService.getUseCoupon(command.getUserId(), item.getProductId());
            }
            createOrder.addOrderItem(product, userCoupon, item.getQuantity());
            
            // Redis에 상품 구매 정보 기록 - 수량을 score로 하여 SortedSet에 저장
            productRankRedisService.recordProductOrder(product.getId(), item.getQuantity());
        }

        DomainOrder order = service.create(createOrder);

        // 결제 처리
        if (order.getTotalPrice().compareTo(totalPrice) > 0) {
            pointService.use(command.getUserId(), totalPrice);
            pointHistoryService.useHistory(command.getUserId(), totalPrice);
        }

        // 외부 데이터 전송
        externalTransmissionService.sendOrderData();
    }

    /*
     * method: createOrderNumber
     * description: 주문 번호 생성
     */
    public String createOrderNumber(){
        LocalDateTime now = LocalDateTime.now();
        return String.format("{0}{1}{2}%08d", now.getYear(), now.getMonth(), now.getDayOfMonth(), (int)(Math.random() * 1_000_000_000) + 1);
    }

    /**
     * 상위 상품 랭킹 조회 (Redis 기반)
     * @return 상위 5개 상품 랭킹 목록
     */
    public List<DomainProductRank> getTopProducts() {
        return productRankRedisService.getTopProducts();
    }

    /**
     * 3일치 데이터 기반 상위 상품 랭킹 조회 (Redis 기반)
     * @return 3일치 데이터 기반 상위 5개 상품 랭킹 목록
     */
    public List<DomainProductRank> getTopProductsThreeDays() {
        return productRankRedisService.getTopProductsThreeDays();
    }

    /**
     * 랭킹 캐시 초기화
     */
    @CacheEvict(value = {"productRanks"}, allEntries = true)
    public void clearRankCache() {
        log.info("상품 랭킹 캐시가 초기화되었습니다.");
    }
}
