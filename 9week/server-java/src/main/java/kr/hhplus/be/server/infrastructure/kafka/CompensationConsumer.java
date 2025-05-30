package kr.hhplus.be.server.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.event.util.CompensationRequired;
import kr.hhplus.be.server.domain.coupon.service.CouponDomainService;
import kr.hhplus.be.server.domain.point.service.PointDomainService;
import kr.hhplus.be.server.domain.product.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationConsumer {
    
    private final ObjectMapper objectMapper;
    private final ProductDomainService productDomainService;
    private final CouponDomainService couponDomainService;
    private final PointDomainService pointDomainService;
    
    @KafkaListener(topics = "compensation-request", groupId = "compensation-service-group")
    @Transactional
    public void handleCompensationRequest(String message) {
        try {
            log.info("보상 트랜잭션 요청 수신 - message: {}", message);
            
            CompensationRequired event = objectMapper.readValue(message, CompensationRequired.class);
            
            log.info("보상 트랜잭션 시작 - orderId: {}, userId: {}, completedActions: {}, reason: {}", 
                    event.getOrderId(), event.getUserId(), event.getCompletedActions(), event.getFailureReason());
            
            // 완료된 작업들에 대해 보상 처리
            for (String action : event.getCompletedActions()) {
                try {
                    switch (action) {
                        case "STOCK_DEDUCTION":
                            compensateStockDeduction(event.getOrderId(), event.getUserId());
                            break;
                        case "COUPON_USAGE":
                            compensateCouponUsage(event.getOrderId(), event.getUserId());
                            break;
                        case "POINT_DEDUCTION":
                            compensatePointDeduction(event.getOrderId(), event.getUserId());
                            break;
                        default:
                            log.warn("알 수 없는 보상 대상 액션 - action: {}, orderId: {}", action, event.getOrderId());
                    }
                } catch (Exception e) {
                    log.error("보상 트랜잭션 처리 중 오류 발생 - action: {}, orderId: {}, userId: {}", 
                            action, event.getOrderId(), event.getUserId(), e);
                }
            }
            
            log.info("보상 트랜잭션 완료 - orderId: {}, userId: {}", event.getOrderId(), event.getUserId());
            
        } catch (JsonProcessingException e) {
            log.error("보상 트랜잭션 요청 JSON 파싱 오류 - message: {}", message, e);
        } catch (Exception e) {
            log.error("보상 트랜잭션 요청 처리 중 예상치 못한 오류 - message: {}", message, e);
        }
    }
    
    /**
     * 재고 차감 보상 (재고 복원)
     */
    private void compensateStockDeduction(Long orderId, Long userId) {
        try {
            // 주문에서 차감된 재고 정보를 조회하여 복원
            // 실제 구현에서는 주문 아이템 정보를 조회해야 함
            boolean success = productDomainService.restoreStockForOrder(orderId);
            
            if (success) {
                log.info("재고 복원 완료 - orderId: {}, userId: {}", orderId, userId);
            } else {
                log.error("재고 복원 실패 - orderId: {}, userId: {}", orderId, userId);
            }
        } catch (Exception e) {
            log.error("재고 복원 중 오류 발생 - orderId: {}, userId: {}", orderId, userId, e);
        }
    }
    
    /**
     * 쿠폰 사용 보상 (쿠폰 복원)
     */
    private void compensateCouponUsage(Long orderId, Long userId) {
        try {
            // 주문에서 사용된 쿠폰을 복원
            boolean success = couponDomainService.restoreCouponForOrder(orderId);
            
            if (success) {
                log.info("쿠폰 복원 완료 - orderId: {}, userId: {}", orderId, userId);
            } else {
                log.error("쿠폰 복원 실패 - orderId: {}, userId: {}", orderId, userId);
            }
        } catch (Exception e) {
            log.error("쿠폰 복원 중 오류 발생 - orderId: {}, userId: {}", orderId, userId, e);
        }
    }
    
    /**
     * 포인트 차감 보상 (포인트 복원)
     */
    private void compensatePointDeduction(Long orderId, Long userId) {
        try {
            // 주문에서 차감된 포인트를 복원
            boolean success = pointDomainService.restorePointForOrder(orderId, userId);
            
            if (success) {
                log.info("포인트 복원 완료 - orderId: {}, userId: {}", orderId, userId);
            } else {
                log.error("포인트 복원 실패 - orderId: {}, userId: {}", orderId, userId);
            }
        } catch (Exception e) {
            log.error("포인트 복원 중 오류 발생 - orderId: {}, userId: {}", orderId, userId, e);
        }
    }
}
