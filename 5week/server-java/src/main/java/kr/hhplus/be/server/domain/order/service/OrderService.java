package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.common.cache.CacheService;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.LockService;
import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.CreateOrderProductHistory;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.infrastructure.order.entity.Order;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderProductHistoryRepository historyRepository;
    private final CacheService cacheService;
    private final LockService lockService;

    /**
     * 주문 생성 메서드 - 분산락 적용
     * 분산락 키: "order:create:{userId}" - 동일 사용자의 주문은 순차적으로 처리함
     */
    @Transactional
    @DistributedLock(key = "#createOrder.userId", prefix = "order:create")
    public DomainOrder create(CreateOrder createOrder) {
        log.info("Creating order for user: {}", createOrder.getUserId());
        
        DomainOrder domainOrder = repository.create(createOrder);
        domainOrder.getItems().forEach(item -> {
            // 상품별 재고 확인 로직 추가 가능
            CreateOrderProductHistory history = new CreateOrderProductHistory(domainOrder.getId(), item.getProductId(), item.getQuantity());
            historyRepository.create(history);
            
            // 캐시 무효화 - 관련 상품의 재고 정보 캐시를 무효화
            cacheService.invalidate("product:stock:" + item.getProductId());
        });
        
        // 캐시 무효화 - 사용자의 이전 주문 목록 캐시 무효화
        cacheService.invalidate("user:orders:" + createOrder.getUserId());
        
        return domainOrder;
    }

    /**
     * 프로그래밍 방식으로 분산락 적용 예시
     */
    @Transactional
    public DomainOrder createWithProgrammaticLock(CreateOrder createOrder) {
        String lockKey = "order:create:" + createOrder.getUserId();
        
        return lockService.executeWithLock(lockKey, () -> {
            DomainOrder domainOrder = repository.create(createOrder);
            domainOrder.getItems().forEach(item -> {
                CreateOrderProductHistory history = new CreateOrderProductHistory(domainOrder.getId(), item.getProductId(), item.getQuantity());
                historyRepository.create(history);
            });
            return domainOrder;
        });
    }

    /**
     * 최근 3일간 주문 상품 이력 조회 - 캐시 적용
     * 자주 조회되지만 실시간성이 중요하지 않은 데이터에 캐시 적용
     */
    @Cacheable(value = "orderProductHistory", key = "'threeDays'")
    public List<OrderHistoryProductGroupVo> threeDaysOrderProductHistory() {
        log.info("Fetching three days order product history from database");
        return historyRepository.findGroupByProductIdThreeDays();
    }
    
    /**
     * 프로그래밍 방식으로 캐시 적용 예시
     */
    public List<OrderHistoryProductGroupVo> threeDaysOrderProductHistoryWithProgrammaticCache() {
        String cacheKey = "orderHistory:threeDays";
        
        return cacheService.getOrCreate(cacheKey, 300, () -> {
            log.info("Cache miss - fetching three days order product history from database");
            return historyRepository.findGroupByProductIdThreeDays();
        });
    }
    
    /**
     * 특정 사용자의 주문 이력 조회 - 캐시 적용
     */
    public List<DomainOrder> getUserOrders(Long userId) {
        String cacheKey = "user:orders:" + userId;
        
        return cacheService.getOrCreate(cacheKey, 60, () -> {
            log.info("Fetching orders for user: {} from database", userId);
            // 실제 사용자 주문 조회 로직
            return repository.findByUserId(userId);
        });
    }
    
    /**
     * 주문 상태 업데이트 - 분산락 적용
     */
    @Transactional
    @DistributedLock(key = "#orderId", prefix = "order:update")
    public void updateOrderStatus(Long orderId, String status) {
        log.info("Updating order status: {} to {}", orderId, status);
        
        // 주문 상태 업데이트 로직
        repository.updateStatus(orderId, status);
        
        // 캐시 무효화
        // 주문 정보 캐시 무효화
        DomainOrder order = repository.findById(orderId);
        if (order != null) {
            cacheService.invalidate("user:orders:" + order.getUserId());
        }
    }
}
