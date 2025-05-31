package kr.hhplus.be.server.common.event.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.event.coupon.CouponUsageCompleted;
import kr.hhplus.be.server.common.event.coupon.CouponUsageFailed;
import kr.hhplus.be.server.common.event.point.PointDeductionCompleted;
import kr.hhplus.be.server.common.event.point.PointDeductionFailed;
import kr.hhplus.be.server.common.event.product.StockDeductionCompleted;
import kr.hhplus.be.server.common.event.product.StockDeductionFailed;
import kr.hhplus.be.server.infrastructure.order.OrderKafkaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventLisner {

    private final ObjectMapper objectMapper;
    private final OrderKafkaRepository orderSagaOrchestrator;
    
    @KafkaListener(topics = "stock-deduction-result", groupId = "order-saga-group")
    public void handleStockDeductionResult(String message) {
        try {
            log.info("재고 차감 결과 수신 - message: {}", message);
            
            JsonNode jsonNode = objectMapper.readTree(message);
            
            if (jsonNode.has("completedAt")) {
                // 성공 이벤트
                StockDeductionCompleted event = objectMapper.readValue(message, StockDeductionCompleted.class);
                orderSagaOrchestrator.handleStockDeductionResult(event);
            } else if (jsonNode.has("failedAt")) {
                // 실패 이벤트
                StockDeductionFailed event = objectMapper.readValue(message, StockDeductionFailed.class);
                orderSagaOrchestrator.handleStockDeductionResult(event);
            }
            
        } catch (JsonProcessingException e) {
            log.error("재고 차감 결과 JSON 파싱 오류 - message: {}", message, e);
        } catch (Exception e) {
            log.error("재고 차감 결과 처리 중 오류 발생 - message: {}", message, e);
        }
    }
    
    @KafkaListener(topics = "coupon-usage-result", groupId = "order-saga-group")
    public void handleCouponUsageResult(String message) {
        try {
            log.info("쿠폰 사용 결과 수신 - message: {}", message);
            
            JsonNode jsonNode = objectMapper.readTree(message);
            
            if (jsonNode.has("completedAt")) {
                // 성공 이벤트
                CouponUsageCompleted event = objectMapper.readValue(message, CouponUsageCompleted.class);
                orderSagaOrchestrator.handleCouponUsageResult(event);
            } else if (jsonNode.has("failedAt")) {
                // 실패 이벤트
                CouponUsageFailed event = objectMapper.readValue(message, CouponUsageFailed.class);
                orderSagaOrchestrator.handleCouponUsageResult(event);
            }
            
        } catch (JsonProcessingException e) {
            log.error("쿠폰 사용 결과 JSON 파싱 오류 - message: {}", message, e);
        } catch (Exception e) {
            log.error("쿠폰 사용 결과 처리 중 오류 발생 - message: {}", message, e);
        }
    }
    
    @KafkaListener(topics = "point-deduction-result", groupId = "order-saga-group")
    public void handlePointDeductionResult(String message) {
        try {
            log.info("포인트 차감 결과 수신 - message: {}", message);
            
            JsonNode jsonNode = objectMapper.readTree(message);
            
            if (jsonNode.has("completedAt")) {
                // 성공 이벤트
                PointDeductionCompleted event = objectMapper.readValue(message, PointDeductionCompleted.class);
                orderSagaOrchestrator.handlePointDeductionResult(event);
            } else if (jsonNode.has("failedAt")) {
                // 실패 이벤트
                PointDeductionFailed event = objectMapper.readValue(message, PointDeductionFailed.class);
                orderSagaOrchestrator.handlePointDeductionResult(event);
            }
            
        } catch (JsonProcessingException e) {
            log.error("포인트 차감 결과 JSON 파싱 오류 - message: {}", message, e);
        } catch (Exception e) {
            log.error("포인트 차감 결과 처리 중 오류 발생 - message: {}", message, e);
        }
    }
}
