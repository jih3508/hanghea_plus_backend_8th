package kr.hhplus.be.server.common.event.point;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.event.product.StockDeductionCompleted;
import kr.hhplus.be.server.common.event.product.StockDeductionFailed;
import kr.hhplus.be.server.common.event.product.StockDeductionRequested;
import kr.hhplus.be.server.common.kafka.TopicType;
import kr.hhplus.be.server.domain.product.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeductionConsumer {
    
    private final ObjectMapper objectMapper;
    private final ProductDomainService productDomainService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @KafkaListener(topics = "stock-deduction-request", groupId = "stock-service-group")
    @Transactional
    public void handleStockDeductionRequest(String message) {
        try {
            log.info("재고 차감 요청 수신 - message: {}", message);
            
            StockDeductionRequested event = objectMapper.readValue(message, StockDeductionRequested.class);
            
            // 재고 차감 처리
            boolean allStockDeducted = true;
            StringBuilder failureReason = new StringBuilder();
            
            for (StockDeductionRequested.OrderItem item : event.getOrderItems()) {
                try {
                    boolean success = productDomainService.deductStock(item.getProductId(), item.getQuantity());
                    if (!success) {
                        allStockDeducted = false;
                        failureReason.append("상품 ID ").append(item.getProductId())
                                   .append(" 재고 부족 (요청 수량: ").append(item.getQuantity()).append("), ");
                    }
                } catch (Exception e) {
                    allStockDeducted = false;
                    failureReason.append("상품 ID ").append(item.getProductId())
                               .append(" 재고 차감 오류: ").append(e.getMessage()).append(", ");
                    log.error("재고 차감 중 오류 발생 - productId: {}, quantity: {}", 
                            item.getProductId(), item.getQuantity(), e);
                }
            }
            
            // 결과 이벤트 발행
            if (allStockDeducted) {
                StockDeductionCompleted completedEvent = StockDeductionCompleted.of(
                    event.getOrderId(), 
                    event.getUserId()
                );
                publishEvent(TopicType.STOCK_DEDUCTION_RESULT.getTopic(), completedEvent);
                
                log.info("재고 차감 성공 - orderId: {}, userId: {}", event.getOrderId(), event.getUserId());
            } else {
                StockDeductionFailed failedEvent = StockDeductionFailed.of(
                    event.getOrderId(), 
                    event.getUserId(), 
                    failureReason.toString()
                );
                publishEvent(TopicType.STOCK_DEDUCTION_RESULT.getTopic(), failedEvent);
                
                log.info("재고 차감 실패 - orderId: {}, userId: {}, reason: {}", 
                        event.getOrderId(), event.getUserId(), failureReason.toString());
            }
            
        } catch (JsonProcessingException e) {
            log.error("재고 차감 요청 JSON 파싱 오류 - message: {}", message, e);
        } catch (Exception e) {
            log.error("재고 차감 요청 처리 중 예상치 못한 오류 - message: {}", message, e);
        }
    }
    
    private void publishEvent(String topic, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, eventJson);
        } catch (JsonProcessingException e) {
            log.error("이벤트 JSON 직렬화 오류 - topic: {}, event: {}", topic, event, e);
        } catch (Exception e) {
            log.error("이벤트 발행 오류 - topic: {}, event: {}", topic, event, e);
        }
    }
}
