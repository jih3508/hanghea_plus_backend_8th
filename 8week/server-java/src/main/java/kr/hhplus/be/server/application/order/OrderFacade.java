package kr.hhplus.be.server.application.order;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.LockStrategy;
import kr.hhplus.be.server.common.lock.LockType;
import kr.hhplus.be.server.common.event.OrderCompletedEvent;
import kr.hhplus.be.server.common.event.OrderCreatedEvent;
import kr.hhplus.be.server.domain.external.ExternalTransmissionService;
import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.domain.order.model.OrderEvent;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.infrastructure.kafka.EventPublisher;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import kr.hhplus.be.server.domain.point.service.PointHistoryService;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.product.model.CreateProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import kr.hhplus.be.server.infrastructure.order.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacade {

    private final OrderService service;

    private final UserService userService;

    private final PointService  pointService;

    private final PointHistoryService pointHistoryService;

    private final ProductService productService;

    private final ProductStockService productStockService;

    private final UserCouponService userCouponService;

    private final ApplicationEventPublisher eventPublisher;

    private final ProductRankService productRankService;
    
    private final EventPublisher kafkaEventPublisher;

    @DistributedLock(key = "#command.getProductIdsAsString()", type = LockType.STOCK, strategy = LockStrategy.PUB_SUB_LOCK)
    @Transactional
    public void order(OrderCommand command) {

        // 회원 존재하는지 여부 확인
        userService.findById(command.getUserId());

        CreateOrder createOrder = new CreateOrder(command.getUserId(), createOrderNumber());

        Map<Long, Integer> beforeProduct = null;
        beforeProduct = new HashMap<>();

        for (OrderCommand.OrderItem item : command.getItems()) {

            DomainProduct product = productService.getProduct(item.getProductId());
            productStockService.delivering(product.getId(), item.getQuantity());


            DomainUserCoupon userCoupon = null;
            if (item.getCouponId() != null) {
                userCoupon = userCouponService.getUseCoupon(command.getUserId(), item.getProductId());
            }
            createOrder.addOrderItem(product, userCoupon, item.getQuantity());
            beforeProduct.put(product.getId(), beforeProduct.getOrDefault(product.getId(), 0) + item.getQuantity());
        }
        DomainOrder order = service.create(createOrder);
        try{

            // 결제 처리
            if (order.getTotalPrice().compareTo(BigDecimal.ZERO) > 0) {
                pointService.use(command.getUserId(), order.getTotalPrice());
                pointHistoryService.useHistory(command.getUserId(), order.getTotalPrice());
            }

            // 외부 데이터 전송 (기존 방식)
            eventPublisher.publishEvent(OrderEvent.created(order));

            // Kafka 이벤트 발행 (새로운 방식)
            publishOrderCreatedEvent(order);
            publishOrderCompletedEvent(order);

        }catch (ApiExceptionResponse e) {
            log.error(e.getMessage(), e);
            // 레디스 랭킹 롤백
            beforeProduct.forEach((id, quantity) -> productRankService.resetRank(id, quantity));
            throw e;
        }

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
     * 주문 랭킹 업데이트 후 캐시 무효화
     * 랭킹 데이터를 업데이트한 후 캐시를 삭제하여 다음 요청 시 최신 데이터가 조회되도록 함
     */
    @CacheEvict(value = "productRanks", key = "'today'")
    public void updateRank(){
        List<OrderHistoryProductGroupVo> list = service.threeDaysOrderProductHistory();
        int size = list.size();
        List<CreateProductRank> productRanks = new LinkedList<>();


        for (int i = 0; i < size; i++) {
            OrderHistoryProductGroupVo vo = list.get(i);
            DomainProduct product = productService.getProduct(vo.productId());

            CreateProductRank rank = CreateProductRank.builder()
                    .productId(product.getId())
                    .totalQuantity(vo.totalQuantity())
                    .rank(i + 1)
                    .build();

            productRanks.add(rank);
        }

        productRankService.save(productRanks);
    }

    /**
     * 주문 생성 이벤트를 Kafka로 발행
     */
    private void publishOrderCreatedEvent(DomainOrder order) {
        List<OrderCreatedEvent.OrderItem> orderItems = order.getOrderItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                orderItems,
                "CREATED"
        );

        // 사용자 ID를 키로 사용하여 같은 사용자의 주문은 같은 파티션으로 전송
        kafkaEventPublisher.publishOrderEvent(order.getUserId().toString(), event);
    }

    /**
     * 주문 완료 이벤트를 Kafka로 발행
     */
    private void publishOrderCompletedEvent(DomainOrder order) {
        OrderCompletedEvent event = new OrderCompletedEvent(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                LocalDateTime.now(),
                "POINT" // 포인트 결제
        );

        // 주문 ID를 키로 사용하여 관련 이벤트들을 순서대로 처리
        kafkaEventPublisher.publishOrderEvent(order.getId().toString(), event);
    }

}
