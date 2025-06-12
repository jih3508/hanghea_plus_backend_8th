package kr.hhplus.be.server.common.event.point;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.kafka.TopicType;
import kr.hhplus.be.server.domain.point.service.PointDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointDeductionConsumer {
    
    private final ObjectMapper objectMapper;
    private final PointDomainService pointDomainService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @KafkaListener(topics = "point-deduction-request", groupId = "point-service-group")
    @Transactional
    public void handlePointDeductionRequest(String message) {
        try {
            log.info("포인트 차감 요청 수신 - message: {}", message);
            
            PointDeductionRequested event = objectMapper.readValue(message, PointDeductionRequested.class);
            
            // 포인트 차감 처리
            try {
                boolean success = pointDomainService.deductPoint(event.getUserId(), event.getPointAmount());
                
                if (success) {
                    PointDeductionCompleted completedEvent = PointDeductionCompleted.of(
                        event.getOrderId(), 
                        event.getUserId(), 
                        event.getPointAmount()
                    );
                    publishEvent(TopicType.POINT_DEDUCTION_RESULT.getTopic(), completedEvent);
                    
                    log.info("포인트 차감 성공 - orderId: {}, userId: {}, amount: {}", 
                            event.getOrderId(), event.getUserId(), event.getPointAmount());
                } else {
                    PointDeductionFailed failedEvent = PointDeductionFailed.of(
                        event.getOrderId(), 
                        event.getUserId(), 
                        event.getPointAmount(),
                        "포인트 잔액 부족"
                    );
                    publishEvent(TopicType.POINT_DEDUCTION_RESULT.getTopic(), failedEvent);
                    
                    log.info("포인트 차감 실패 - orderId: {}, userId: {}, amount: {} (잔액 부족)", 
                            event.getOrderId(), event.getUserId(), event.getPointAmount());
                }
                
            } catch (Exception e) {
                PointDeductionFailed failedEvent = PointDeductionFailed.of(
                    event.getOrderId(), 
                    event.getUserId(), 
                    event.getPointAmount(),
                    "포인트 차감 중 오류 발생: " + e.getMessage()
                );
                publishEvent(TopicType.POINT_DEDUCTION_RESULT.getTopic(), failedEvent);
                
                log.error("포인트 차감 중 오류 발생 - orderId: {}, userId: {}, amount: {}", 
                        event.getOrderId(), event.getUserId(), event.getPointAmount(), e);
            }
            
        } catch (JsonProcessingException e) {
            log.error("포인트 차감 요청 JSON 파싱 오류 - message: {}", message, e);
        } catch (Exception e) {
            log.error("포인트 차감 요청 처리 중 예상치 못한 오류 - message: {}", message, e);
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
