package kr.hhplus.be.server.infrastructure.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.event.util.CompensationRequired;
import kr.hhplus.be.server.common.event.coupon.CouponUsageCompleted;
import kr.hhplus.be.server.common.event.coupon.CouponUsageFailed;
import kr.hhplus.be.server.common.event.coupon.CouponUsageRequested;
import kr.hhplus.be.server.common.event.point.PointDeductionCompleted;
import kr.hhplus.be.server.common.event.point.PointDeductionFailed;
import kr.hhplus.be.server.common.event.point.PointDeductionRequested;
import kr.hhplus.be.server.common.event.product.StockDeductionCompleted;
import kr.hhplus.be.server.common.event.product.StockDeductionFailed;
import kr.hhplus.be.server.common.event.product.StockDeductionRequested;
import kr.hhplus.be.server.common.kafka.TopicType;
import kr.hhplus.be.server.domain.order.service.OrderDomainService;
import kr.hhplus.be.server.infrastructure.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaRepository {
    
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final OrderDomainService orderDomainService;
    private final WebSocketNotificationService webSocketNotificationService;
    
    private static final String SAGA_STATE_PREFIX = "order:saga:";
    private static final int SAGA_STATE_EXPIRE_SECONDS = 3600; // 1시간
    
    /**
     * 주문 사가 시작 - 각 서비스에 병렬로 요청 발송
     */
    public void startOrderSaga(Long orderId, Long userId, List<StockDeductionRequested.OrderItem> orderItems,
                              Long userCouponId, java.math.BigDecimal pointAmount) {
        try {
            log.info("주문 사가 시작 - orderId: {}, userId: {}", orderId, userId);
            
            // 사가 상태 초기화
            initializeSagaState(orderId);
            
            // 병렬로 이벤트 발행
            publishStockDeductionRequest(orderId, userId, orderItems);
            
            if (userCouponId != null) {
                publishCouponUsageRequest(orderId, userId, userCouponId);
            } else {
                // 쿠폰 사용하지 않는 경우 완료로 표시
                updateSagaState(orderId, "COUPON_USAGE", "COMPLETED");
            }
            
            if (pointAmount != null && pointAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                publishPointDeductionRequest(orderId, userId, pointAmount);
            } else {
                // 포인트 사용하지 않는 경우 완료로 표시
                updateSagaState(orderId, "POINT_DEDUCTION", "COMPLETED");
            }
            
        } catch (Exception e) {
            log.error("주문 사가 시작 중 오류 발생 - orderId: {}", orderId, e);
            handleSagaFailure(orderId, userId, "사가 시작 실패: " + e.getMessage());
        }
    }
    
    /**
     * 재고 차감 결과 처리
     */
    public void handleStockDeductionResult(Object event) {
        if (event instanceof StockDeductionCompleted) {
            StockDeductionCompleted completed = (StockDeductionCompleted) event;
            updateSagaState(completed.getOrderId(), "STOCK_DEDUCTION", "COMPLETED");
            checkSagaCompletion(completed.getOrderId(), completed.getUserId());
        } else if (event instanceof StockDeductionFailed) {
            StockDeductionFailed failed = (StockDeductionFailed) event;
            updateSagaState(failed.getOrderId(), "STOCK_DEDUCTION", "FAILED");
            handleSagaFailure(failed.getOrderId(), failed.getUserId(), failed.getReason());
        }
    }
    
    /**
     * 쿠폰 사용 결과 처리
     */
    public void handleCouponUsageResult(Object event) {
        if (event instanceof CouponUsageCompleted) {
            CouponUsageCompleted completed = (CouponUsageCompleted) event;
            updateSagaState(completed.getOrderId(), "COUPON_USAGE", "COMPLETED");
            checkSagaCompletion(completed.getOrderId(), completed.getUserId());
        } else if (event instanceof CouponUsageFailed) {
            CouponUsageFailed failed = (CouponUsageFailed) event;
            updateSagaState(failed.getOrderId(), "COUPON_USAGE", "FAILED");
            handleSagaFailure(failed.getOrderId(), failed.getUserId(), failed.getReason());
        }
    }
    
    /**
     * 포인트 차감 결과 처리
     */
    public void handlePointDeductionResult(Object event) {
        if (event instanceof PointDeductionCompleted) {
            PointDeductionCompleted completed = (PointDeductionCompleted) event;
            updateSagaState(completed.getOrderId(), "POINT_DEDUCTION", "COMPLETED");
            checkSagaCompletion(completed.getOrderId(), completed.getUserId());
        } else if (event instanceof PointDeductionFailed) {
            PointDeductionFailed failed = (PointDeductionFailed) event;
            updateSagaState(failed.getOrderId(), "POINT_DEDUCTION", "FAILED");
            handleSagaFailure(failed.getOrderId(), failed.getUserId(), failed.getReason());
        }
    }
    
    private void initializeSagaState(Long orderId) {
        String key = SAGA_STATE_PREFIX + orderId;
        redisTemplate.opsForHash().put(key, "STOCK_DEDUCTION", "PENDING");
        redisTemplate.opsForHash().put(key, "COUPON_USAGE", "PENDING");
        redisTemplate.opsForHash().put(key, "POINT_DEDUCTION", "PENDING");
        redisTemplate.expire(key, SAGA_STATE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }
    
    private void updateSagaState(Long orderId, String action, String status) {
        String key = SAGA_STATE_PREFIX + orderId;
        redisTemplate.opsForHash().put(key, action, status);
        log.info("사가 상태 업데이트 - orderId: {}, action: {}, status: {}", orderId, action, status);
    }
    
    private void checkSagaCompletion(Long orderId, Long userId) {
        String key = SAGA_STATE_PREFIX + orderId;
        
        // 모든 상태 조회
        String stockStatus = (String) redisTemplate.opsForHash().get(key, "STOCK_DEDUCTION");
        String couponStatus = (String) redisTemplate.opsForHash().get(key, "COUPON_USAGE");
        String pointStatus = (String) redisTemplate.opsForHash().get(key, "POINT_DEDUCTION");
        
        log.info("사가 완료 체크 - orderId: {}, stock: {}, coupon: {}, point: {}", 
                orderId, stockStatus, couponStatus, pointStatus);
        
        // 모든 작업이 완료되었는지 확인
        if ("COMPLETED".equals(stockStatus) && "COMPLETED".equals(couponStatus) && "COMPLETED".equals(pointStatus)) {
            // 주문 완료 처리
            orderDomainService.completeOrder(orderId);
            
            // WebSocket 알림
            webSocketNotificationService.notifyOrderCompleted(userId, orderId);
            
            // 사가 상태 정리
            redisTemplate.delete(key);
            
            log.info("주문 사가 완료 - orderId: {}, userId: {}", orderId, userId);
        }
        // 실패한 작업이 있는지 확인
        else if ("FAILED".equals(stockStatus) || "FAILED".equals(couponStatus) || "FAILED".equals(pointStatus)) {
            // 이미 handleSagaFailure에서 처리됨
            log.info("사가 실패 감지됨 - orderId: {}", orderId);
        }
    }
    
    private void handleSagaFailure(Long orderId, Long userId, String reason) {
        try {
            String key = SAGA_STATE_PREFIX + orderId;
            
            // 완료된 작업들 조회 (보상 대상)
            List<String> completedActions = new ArrayList<>();
            String stockStatus = (String) redisTemplate.opsForHash().get(key, "STOCK_DEDUCTION");
            String couponStatus = (String) redisTemplate.opsForHash().get(key, "COUPON_USAGE");
            String pointStatus = (String) redisTemplate.opsForHash().get(key, "POINT_DEDUCTION");
            
            if ("COMPLETED".equals(stockStatus)) {
                completedActions.add("STOCK_DEDUCTION");
            }
            if ("COMPLETED".equals(couponStatus)) {
                completedActions.add("COUPON_USAGE");
            }
            if ("COMPLETED".equals(pointStatus)) {
                completedActions.add("POINT_DEDUCTION");
            }
            
            // 보상 트랜잭션 요청
            if (!completedActions.isEmpty()) {
                CompensationRequired compensationEvent = CompensationRequired.of(orderId, userId, completedActions, reason);
                publishEvent(TopicType.COMPENSATION_REQUEST.getTopic(), compensationEvent);
            }
            
            // 주문 취소 처리
            orderDomainService.cancelOrder(orderId, reason);
            
            // WebSocket 알림
            webSocketNotificationService.notifyOrderFailed(userId, orderId, reason);
            
            // 사가 상태 정리
            redisTemplate.delete(key);
            
            log.info("주문 사가 실패 처리 완료 - orderId: {}, userId: {}, reason: {}", orderId, userId, reason);
            
        } catch (Exception e) {
            log.error("사가 실패 처리 중 오류 발생 - orderId: {}, userId: {}", orderId, userId, e);
        }
    }
    
    private void publishStockDeductionRequest(Long orderId, Long userId, List<StockDeductionRequested.OrderItem> orderItems) {
        StockDeductionRequested event = StockDeductionRequested.of(orderId, userId, orderItems);
        publishEvent(TopicType.STOCK_DEDUCTION_REQUEST.getTopic(), event);
    }
    
    private void publishCouponUsageRequest(Long orderId, Long userId, Long userCouponId) {
        CouponUsageRequested event = CouponUsageRequested.of(orderId, userId, userCouponId);
        publishEvent(TopicType.COUPON_USAGE_REQUEST.getTopic(), event);
    }
    
    private void publishPointDeductionRequest(Long orderId, Long userId, java.math.BigDecimal pointAmount) {
        PointDeductionRequested event = PointDeductionRequested.of(orderId, userId, pointAmount);
        publishEvent(TopicType.POINT_DEDUCTION_REQUEST.getTopic(), event);
    }
    
    private void publishEvent(String topic, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, eventJson);
            log.info("이벤트 발행 완료 - topic: {}, event: {}", topic, event.getClass().getSimpleName());
        } catch (JsonProcessingException e) {
            log.error("이벤트 JSON 직렬화 오류 - topic: {}, event: {}", topic, event, e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }
}
